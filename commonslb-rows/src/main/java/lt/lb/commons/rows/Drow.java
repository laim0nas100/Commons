package lt.lb.commons.rows;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lt.lb.commons.ArrayOp;
import lt.lb.commons.F;
import lt.lb.commons.containers.tuples.Tuple;
import lt.lb.commons.containers.tuples.Tuples;
import lt.lb.commons.iteration.ReadOnlyIterator;
import lt.lb.commons.misc.NestedException;

/**
 *
 * @author laim0nas100
 */
public abstract class Drow<C, N, L, U extends Updates<U>, Conf extends DrowConf<R, C, N, L, U>, R extends Drow> {

    protected boolean visible = true;
    protected boolean disabled = false;
    protected boolean deleted = false;

    protected boolean displayed = false;

    protected List<Integer> cellColSpan = new ArrayList<>();
    protected List<C> cells = new ArrayList<>();
    protected Conf config;
    protected L line;
    protected String key;
    protected Map<String, U> updates = new HashMap<>();
    protected Set<String> tags = new HashSet<>();
    protected Queue<OrderedConsumer<R>> updateListeners = new PriorityQueue<>();

    /**
     * Everything to do during display. Every component should exist as an
     * object, but still not rendered. This is used to give styles, final
     * touches, decoration, etc. Called usually only once, unless the row was
     * cleared. Followed by UPDATES_ON_REFRESH.
     */
    public static final String UPDATES_ON_DISPLAY = "UPDATES_ON_DISPLAY";
    /**
     * Everything to do after anything to do with this row or components
     * changed, followed by UPDATES_ON_RENDER
     */
    public static final String UPDATES_ON_REFRESH = "UPDATES_ON_REFRESH";

    /**
     * Everything to after update, basically rendering the updated row
     */
    public static final String UPDATES_ON_RENDER = "UPDATES_ON_RERENDER";

    /**
     * Everything to do after visible property changed, followed by
     * UPDATES_ON_REFRESH
     */
    public static final String UPDATES_ON_VISIBLE = "UPDATES_ON_VISIBLE";
    /**
     * Everything to do after disabled property changed, followed by
     * UPDATES_ON_REFRESH
     */
    public static final String UPDATES_ON_DISABLE = "UPDATES_ON_DISABLE";

    public Drow(L line, Conf config, String key) {
        this.config = config;
        this.key = key;
        this.line = line;

        config.configureUpdates(updates, me());

    }

    public void initUpdates() {

        for (String type : defaultUpdateName()) {
            updates.put(type, config.createUpdates(type, me()));
        }

        U u_render = updates.get(UPDATES_ON_RENDER);
        U u_refresh = updates.get(UPDATES_ON_REFRESH);
        U u_display = updates.get(UPDATES_ON_DISPLAY);
        U u_disable = updates.get(UPDATES_ON_DISABLE);
        U u_visible = updates.get(UPDATES_ON_VISIBLE);

        u_render.addUpdate(() -> {
            reorderColSpans();
            config.renderRow(me());
        });

        u_refresh.addUpdate(() -> {
            updateListeners.forEach(action -> {
                action.accept(me());
            });
        });

        u_refresh.addFollowUp(u_render);
        u_display.addFollowUp(u_refresh);
        u_disable.addFollowUp(u_refresh);
        u_visible.addFollowUp(u_refresh);

    }

    public String[] defaultUpdateName() {
        return ArrayOp.asArray(UPDATES_ON_DISPLAY,
                UPDATES_ON_REFRESH,
                UPDATES_ON_RENDER,
                UPDATES_ON_VISIBLE,
                UPDATES_ON_DISABLE
        );
    }

    public List<Integer> getPreferedColSpan() {
        return this.cellColSpan;
    }

    public boolean isVisible() {
        return visible;
    }

    public R setVisible(boolean visible) {
        this.visible = visible;
        return update(UPDATES_ON_VISIBLE);
    }

    public boolean isDisabled() {
        return disabled;
    }

    public R setDisabled(boolean disabled) {
        this.disabled = disabled;
        return update(UPDATES_ON_DISABLE);
    }

    public boolean isDeleted() {
        return deleted;
    }

    public R setDeleted(boolean deleted) {
        this.deleted = deleted;
        return me();
    }

    public boolean isDisplayed() {
        return displayed;
    }

    public boolean isRendable() {
        return isDisplayed() && isVisible() && !isDeleted();
    }

    public L getLine() {
        return line;
    }

    public String getKey() {
        return key;
    }

    public List<C> getCells() {
        return this.cells;
    }

    public CellInfo<C, N> getCellInfo() {
        return config.getCellInfo(me());
    }

    public NodeInfo<N> getNodeInfo() {
        return config.getNodeInfo(me());
    }

    public int getTotalColSpan() {
        return getCells().stream().mapToInt(m -> getCellInfo().getColSpan(m)).sum();
    }

    public int getTotalColSpanVisible() {
        return getVisibleCells().stream().mapToInt(m -> getCellInfo().getColSpan(m)).sum();
    }

    public List<C> getVisibleCells() {
        return getCells().stream().filter(c -> getCellInfo().isVisible(c)).collect(Collectors.toList());
    }

    public List<Integer> getVisibleIndices() {
        ArrayList<Integer> list = new ArrayList<>();
        F.iterate(cells, (i, cell) -> {
            if (getCellInfo().isVisible(cell)) {
                list.add(i);
            }
        });

        return list;
    }

    public List<Integer> getPreferedColSpanOfVisible() {
        ArrayList<Integer> prefVis = new ArrayList<>();
        F.iterate(cells, (i, cell) -> {
            if (getCellInfo().isVisible(cell)) {
                prefVis.add(getPreferedColSpan().get(i));
            }
        });
        return prefVis;
    }

    public C getCell(int index) {
        return cells.get(index);
    }

    public boolean needUpdate(int maxVisibleRowSpan) {
        int totalVisible = this.getTotalColSpanVisible();

        if (maxVisibleRowSpan != totalVisible || totalVisible > config.getMaxColSpan(me())) {
            return true;
        }
        //calculate real colspans
        List<Integer> cellIndex = this.getVisibleIndices();
        int count = cellIndex.size();

        List<Integer> preferedColSpanOfVisible = this.getPreferedColSpanOfVisible();
        double total = preferedColSpanOfVisible.stream().mapToInt(m -> m).sum();

        for (int i = 0; i < count; i++) {
            double ratioPrefered = preferedColSpanOfVisible.get(i) / total;
            double ratioCurrent = getCellInfo().getColSpan(getCell(cellIndex.get(i))) / totalVisible;
            if (!config.colspanWithinMargin(Math.abs(ratioCurrent - ratioPrefered), me())) {
                return true;
            }

        }
        return false;
    }

    public void reorderColSpans() {

        int maxColSpan = config.getMaxColSpan(me());
        if (!this.needUpdate(maxColSpan)) {
            return;
        }
        List<C> visibleCells = this.getVisibleCells();
        if (visibleCells.isEmpty()) {
            return;
        }
        List<Integer> preferedColSpan = this.getPreferedColSpanOfVisible();
        double preferedTotal = preferedColSpan.stream().mapToDouble(m -> m.doubleValue()).sum();
        double mult = maxColSpan / preferedTotal;

        Integer[] colApply = new Integer[preferedColSpan.size()];
        F.iterate(preferedColSpan, (i, pref) -> {
            colApply[i] = (int) Math.floor(pref * mult);
        });

        int newTotalColspan = Stream.of(colApply).mapToInt(m -> m).sum();

        int surplus = maxColSpan - newTotalColspan;

        while (surplus > 0) {
            for (int i = 0; i < colApply.length; i++) {
                if (surplus <= 0) {
                    break;
                }
                colApply[i] += 1;
                surplus--;
            }
        }

        for (int i = 0; i < colApply.length; i++) {
            getCellInfo().setColSpan(visibleCells.get(i), colApply[i]);
        }
    }

    protected abstract R me();

    public R add(N node) {
        R me = me();
        finalAdd(Arrays.asList(node), config.getEnclosingNode(me), cells, 1, cellColSpan);
        return me;
    }

    public R mergeLast(Supplier<? extends N> enclosing, int lastCount) {
        int[] indexes = new int[lastCount];
        int lastIndex = this.getNodeCount() - 1;

        for (int i = 0; i < lastCount; i++) {
            int j = indexes.length - 1 - i;
            indexes[j] = lastIndex;
            lastIndex--;

        }

        return merge(enclosing, indexes);

    }

    public R merge(Supplier<? extends N> enclosing, int... comps) {
        if (comps.length <= 1) {// no op
            return me();
        }
        for (int i = 1; i < comps.length; i++) {
            int prev = comps[i - 1];
            if (prev + 1 != comps[i]) { // sequential
                throw new IllegalArgumentException("Only sequential merging is allowed");
            }
        }
        for (int i = 0; i < comps.length; i++) {
            final int num = comps[i];
            C get = getCellSupplier(p -> p.g1 == num).get();

            if (getCellInfo().isMerged(get)) {
                throw new IllegalArgumentException("node num:" + num + " is part of a merged cell, can only merge free cells");
            }
//            if (get.getChildren().get(0).getChildren().size() != 1) {
//                throw new IllegalArgumentException("component num:" + num + " is part of a merged cell, can only merge free cells");
//            }
        }

        ArrayList<Tuple<Integer, C>> before = new ArrayList<>();
        ArrayList<Tuple<Integer, C>> merge = new ArrayList<>();
        ArrayList<Tuple<Integer, C>> after = new ArrayList<>();

        ArrayList<C> newCells = new ArrayList<>();
        ArrayList<Integer> newColSpans = new ArrayList<>();

        int beforeIndex = comps[0];
        int afterIndex = comps[comps.length - 1];

        F.iterate(mainIterator(), (i, tup) -> {
            C cell = tup.g1;
            int cellIndex = cells.indexOf(cell);
            Integer colSpan = cellColSpan.get(cellIndex);
            Tuple<Integer, C> tuple = Tuples.create(colSpan, cell);

            if (i < beforeIndex) {
                if (!before.contains(tuple)) {
                    before.add(tuple);

                }

            } else if (i <= afterIndex) {
                //should not be shared cells, because we checked before
                merge.add(tuple);
            } else if (i > afterIndex) {
                if (!after.contains(tuple)) {
                    after.add(tuple);
                }
            }

        });
        //insert before merged

        F.iterate(before, (i, tup) -> {
            newCells.add(tup.getG2());
            newColSpans.add(tup.g1);
        });

        int mergedColspan = merge.stream().mapToInt(m -> m.g1).sum();
        List<N> mergedNodes = merge.stream()
                .flatMap(m -> getCellInfo().getNodes(m.g2).stream())
                .collect(Collectors.toList());

        this.finalAdd(mergedNodes, enclosing.get(), newCells, mergedColspan, newColSpans);

        //insert after merged
        F.iterate(after, (i, tup) -> {
            newCells.add(tup.getG2());
            newColSpans.add(tup.g1);
        });

        this.cells = newCells;
        this.cellColSpan = newColSpans;

        return me();

    }

    protected void finalAdd(List<N> nodes, N enclosing, List<C> cellArray, int colSpan, List<Integer> cellColSpanArray) {

        C cell = config.createCell(nodes, enclosing, me());
        int indexToAdd = cellArray.size();
        cellArray.add(cell);
        getCellInfo().setColSpan(cell, colSpan);

        while (indexToAdd >= cellColSpanArray.size()) {
            cellColSpanArray.add(colSpan);
        }

    }

    public R withUpdates(U update) {
        R me = me();
        updates.put(update.type, update);
        return me;
    }

    public R withUpdate(String type, Consumer<? super R> update) {
        R me = me();
        updates.computeIfAbsent(type, t -> config.createUpdates(type, me)).addUpdate(() -> update.accept(me));
        return me;
    }

    public R update(String type) {
        R me = me();
        U get = this.updates.getOrDefault(type, null);

        if (get == null) {
            return me;
        }

        this.config.doUpdates(get, me);

        this.update();
        return me;
    }

    protected R doUpdates(String type) {
        R me = me();
        config.doUpdates(updates.get(type), me);
        return me;
    }

    public R update() {
        return doUpdates(UPDATES_ON_REFRESH);
    }

    public R withPreferedColspan(Integer... spans) {
        R me = me();
        F.iterate(spans, (i, spa) -> {
            if (me.cellColSpan.size() <= i) {
                me.cellColSpan.add(spa);
            } else {
                cellColSpan.set(i, spa);
            }
            if (displayed) {
                C cell = this.getCell(i);
                getCellInfo().setColSpan(cell, spa);
            }

        });
        if (displayed) {
            update();
        }

        return me;
    }

    public int getNodeCount() {
        return cells.stream().mapToInt(m -> getCellInfo().getNodes(m).size()).sum();
    }

    private ReadOnlyIterator<Tuple<C, N>> mainIterator() {
        ArrayList<C> cellCopy = new ArrayList<>(cells);
        int total = getNodeCount();
        return new ReadOnlyIterator<Tuple<C, N>>() {
            int cellIndex = 0;
            int compIndex = -1;
            int totalIndex = -1;

            private C getCurrentCell() {
                return cellCopy.get(cellIndex);
            }

            private N getCurrentComp() {
                return getCellInfo().getNodes(getCurrentCell()).get(compIndex);
            }

            @Override
            public Tuple<C, N> getCurrent() {
                return totalIndex >= 0 ? Tuples.create(getCurrentCell(), getCurrentComp()) : null;
            }

            @Override
            public Integer getCurrentIndex() {
                return totalIndex;
            }

            @Override
            public boolean hasNext() {
                return totalIndex + 1 < total;
            }

            private int currentCellChildrenSize() {
                return getCellInfo().getNodes(getCurrentCell()).size();
            }

            @Override
            public Tuple<C, N> next() {
                if (hasNext()) {

                    if (compIndex + 1 < currentCellChildrenSize()) { // must get component from this cell
                        compIndex++;
                        totalIndex++;
                        return getCurrent();
                    } else { // increment cell index
                        compIndex = 0;
                        cellIndex++;
                        totalIndex++;
                        return getCurrent();
                    }
                } else {
                    throw new NoSuchElementException();
                }

            }

            @Override
            public void close() {
            }
        };
    }

    public List<N> getNodes() {
        return mainIterator().map(m -> m.g2).toArrayList();
    }

    public Supplier<C> getCellSupplier(Predicate<Tuple<Integer, N>> pred) {
        return () -> {
            return F.find(mainIterator(), (i, tuple) -> pred.test(Tuples.create(i, tuple.g2)))
                    .map(m -> m.g2.g1).orElse(null);
        };
    }

    public Supplier<N> getNodeSupplier(Integer i) {
        return () -> {
            return F.find(mainIterator(), (j, tuple) -> Objects.equals(i, j))
                    .map(m -> m.g2.g2).orElse(null);
        };
    }

    public Supplier<N> getNodeSupplier(Predicate<N> pred) {

        return () -> {
            return F.find(mainIterator(), (j, tuple) -> pred.test(tuple.g2))
                    .map(m -> m.g2.g2).orElse(null);
        };
    }

    public <T extends N> T getNode(Predicate<N> pred) {
        return F.cast(getNodeSupplier(pred).get());
    }

    public <T extends N> T getNode(Integer i) {
        return F.cast(getNodeSupplier(i).get());
    }

    public R display() {

        R me = me();
        if (!displayed) {
            Optional<Throwable> optionalException = F.checkedRun(() -> {
                doUpdates(UPDATES_ON_DISPLAY);

            });
            optionalException.ifPresent(ex -> {
                throw NestedException.of(ex);
            });
            displayed = true;
            this.update();
        }
        return me;

    }

    public R withTag(String... tag) {
        for (String t : tag) {
            this.tags.add(t);
        }
        return me();
    }

    public boolean hasTag(String tag) {
        return tags.contains(tag);
    }

    public R clear() {
        displayed = false;
        cells.clear();
        return me();
    }

    public R withNodeDecorator(int index, BiConsumer<R, N>... cons) {
        return addOnDisplayAndRunIfDone(() -> {
            N node = getNode(index);
            R me = me();
            for (BiConsumer<R, N> con : cons) {
                con.accept(me, node);
            }
        });
    }

    public R withNodeDecorator(int index, Consumer<N>... cons) {
        return addOnDisplayAndRunIfDone(() -> {
            N node = getNode(index);
            for (Consumer<N> con : cons) {
                con.accept(node);
            }
        });
    }

    public R withRowDecorator(Consumer<R>... decorator) {
        R me = me();
        for (Consumer<R> dec : decorator) {
            dec.accept(me);
        }
        return me;
    }

    public R bindDefaultUpdatesFrom(R source) {
        R me = me();
        source.bindDefaultUpdates(me);
        return me;
    }

    public R bindUpdatesFrom(String type, R source) {
        R me = me();
        source.bindUpdates(type, me);
        return me;
    }

    public R bindUpdates(String type, R dest) {
        U u = this.updates.get(type);
        U de = (U) dest.updates.get(type);
        u = u.bindPropogate(de);
        this.updates.put(type, u);
        return me();
    }

    public R bindDefaultUpdates(R dest) {
        for (String type : defaultUpdateName()) {
            bindUpdates(type, dest);
        }
        return me();
    }

    public R withUpdateListener(Consumer<R> listener) {
        this.updateListeners.add(new OrderedConsumer<>(listener));
        return me();
    }

    public R withUpdateListener(int order, Consumer<R> listener) {
        this.updateListeners.add(new OrderedConsumer<>(order, listener));
        return me();
    }

    private R addOnDisplayAndRunIfDone(Runnable run) {
        if (displayed) {
            run.run();
        }
        updates.get(UPDATES_ON_DISPLAY).addUpdate(run);
        return me();
    }

}