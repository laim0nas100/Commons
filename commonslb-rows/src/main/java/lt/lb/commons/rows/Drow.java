package lt.lb.commons.rows;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lt.lb.commons.F;
import lt.lb.commons.Ins;
import lt.lb.commons.containers.tuples.Tuple;
import lt.lb.commons.containers.tuples.Tuples;
import lt.lb.commons.iteration.For;
import lt.lb.commons.iteration.ReadOnlyIterator;
import lt.lb.commons.misc.RecursiveRedirection;
import static lt.lb.commons.rows.BasicUpdates.*;
import lt.lb.uncheckedutils.Checked;
import lt.lb.uncheckedutils.NestedException;

/**
 *
 * @author laim0nas100
 * @param <C>
 * @param <N>
 * @param <L>
 * @param <U>
 * @param <Conf>
 * @param <R>
 */
public abstract class Drow<C extends CellInfo<N>, N, L, U extends Updates<U>, Conf extends DrowConf<R, C, N, L, U>, R extends Drow> implements UpdateAware<U, R> {

    protected boolean visible = true;
    protected boolean disabled = false;
    protected boolean deleted = false;

    protected boolean displayed = false;

    protected List<C> cells = new ArrayList<>();
    protected Conf config;
    protected L line;
    protected String key;
    protected Map<String, U> updates = new HashMap<>();
    protected Set<String> tags;

    @Override
    public Conf getConfig() {
        return config;
    }

    /**
     * In ON_RENDER event, the order at which rendering is processed.
     *
     * Can add more events after or before that.
     *
     * After derender, do the deletion of the row, if it's marked as deleted.
     * Only <b>active</b> rows are processed, so if you delete a row, it must be
     * derendered and only then deactivated and cleaned up.
     *
     * @return
     */
    public int getRenderOrder() {
        return 10000;
    }

    /**
     * In ON_RENDER event, the order at which deletion is processed.
     *
     *
     * Can add more events after or before that.
     *
     * After derender, do the deletion of the row, if it's marked as deleted.
     * Only <b>active</b> rows are processed, so if you delete a row, it must be
     * derendered and only then deactivated and cleaned up. Must be bigger than
     * render order.
     *
     * @return
     */
    public int getDeleteOrder() {
        return getRenderOrder() + 10000;
    }

    public Drow(L line, Conf config, String key) {
        this.config = config;
        this.key = key;
        this.line = line;

        config.configureUpdates(updates, me());

    }

    @Override
    public R initUpdates() {

        for (String type : defaultUpdateNames()) {
            updates.put(type, config.createUpdates(type, me()));
        }

        U u_render = updates.get(UPDATES_ON_RENDER);
        U u_refresh = updates.get(UPDATES_ON_REFRESH);
        U u_display = updates.get(UPDATES_ON_DISPLAY);
        U u_disable = updates.get(UPDATES_ON_DISABLE);
        U u_visible = updates.get(UPDATES_ON_VISIBLE);

        u_render.addUpdate(getRenderOrder(), () -> {
            reorderColSpans();
            config.renderRow(me());
        });

        u_display.addFollowUp(u_refresh);
        u_disable.addFollowUp(u_refresh);
        u_visible.addFollowUp(u_refresh);
        return me();

    }

    @Override
    public Map<String, U> getUpdateMap() {
        return this.updates;
    }

    public List<Integer> getPreferedColSpan() {
        return this.cells.stream().map(m -> m.getColSpan()).collect(Collectors.toList());
    }

    public boolean isVisible() {
        return visible;
    }

    public R setVisible(boolean visible) {
        this.visible = visible;
        return update(UPDATES_ON_VISIBLE);
    }

    /**
     * Whether this row is currently disabled.
     *
     * @return
     */
    public boolean isDisabled() {
        return disabled;
    }

    /**
     * Set the disabled status. Also fires all the updates that are configured
     * for disabled event.
     *
     * @param disabled
     * @return
     */
    public R setDisabled(boolean disabled) {
        this.disabled = disabled;
        return update(UPDATES_ON_DISABLE);
    }

    /**
     * Sets the deleted status. Usually don't use this, because it is used by
     * parent object that is managing the rows.
     *
     * @return
     */
    public boolean isDeleted() {
        return deleted;
    }

    /**
     * Will delete after this row calls render, because there might be some
     * nodes that need to be de-rendered before the deletion.
     *
     * @return
     */
    public R delete() {
        this.deleted = true;
        return me();
    }

    /**
     * Whether this row has been displayed (method {@link #display(boolean)} or
     * {@link #display} has been called)
     *
     * @return
     */
    public boolean isDisplayed() {
        return displayed;
    }

    /**
     *
     * Visible, displayed and not deleted.
     *
     * @return
     */
    public boolean isActive() {
        return isDisplayed() && isVisible() && !isDeleted();
    }

    /**
     * A render component that is associated with this row
     *
     * @return
     */
    public L getLine() {
        return line;
    }

    /**
     * Unique key of this row.
     *
     * @return
     */
    public String getKey() {
        return key;
    }

    /**
     * Gets working list of cells, can modify directly.
     *
     * @return
     */
    public List<C> getCells() {
        return this.cells;
    }

    /**
     * Gets a sum of colspan of each cell in this row.
     *
     * @return
     */
    public int getTotalColSpan() {
        return getCells().stream().mapToInt(m -> m.getColSpan()).sum();
    }

    /**
     * Gets a sum of colspan of each visible cell in this row.
     *
     * @return
     */
    public int getTotalColSpanVisible() {
        return getVisibleCells().stream().mapToInt(m -> m.getColSpan()).sum();
    }

    /**
     * Gets a List of visible cells
     *
     * @return
     */
    public List<C> getVisibleCells() {
        return getCells().stream().filter(c -> c.isVisible()).collect(Collectors.toList());
    }

    /**
     * Gets a List of indices of cells that are visible in this row
     *
     * @return
     */
    public List<Integer> getVisibleIndices() {
        ArrayList<Integer> list = new ArrayList<>();
        For.elements().iterate(cells, (i, cell) -> {
            if (cell.isVisible()) {
                list.add(i);
            }
        });

        return list;
    }

    public List<Integer> getPreferedColSpanOfVisible() {
        ArrayList<Integer> prefVis = new ArrayList<>();
        List<Integer> preferedColSpan = getPreferedColSpan();
        For.elements().iterate(cells, (i, cell) -> {
            if (cell.isVisible()) {
                prefVis.add(preferedColSpan.get(i));
            }
        });
        return prefVis;
    }

    /**
     * Returns the working cell at given index.
     *
     * @param index
     * @return
     */
    public C getCell(int index) {
        return cells.get(index);
    }

    /**
     * System method, usually don't use this. Decide whether row needs update
     * the column spans of the cells.
     *
     * @param maxVisibleRowSpan
     * @return
     */
    protected boolean needUpdate(int maxVisibleRowSpan) {
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
            double ratioCurrent = getCell(cellIndex.get(i)).getColSpan() / totalVisible;
            if (!config.colspanWithinMargin(Math.abs(ratioCurrent - ratioPrefered), me())) {
                return true;
            }

        }
        return false;
    }

    /**
     * System method, usually don't use this. Rewrite colspans of the cells,
     * based on surplus redistributing policy.
     */
    protected void reorderColSpans() {

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
        For.elements().iterate(preferedColSpan, (i, pref) -> {
            colApply[i] = (int) Math.floor(pref * mult);
        });

        int newTotalColspan = Stream.of(colApply).mapToInt(m -> m).sum();

        int surplus = maxColSpan - newTotalColspan;

        For.elements().iterate(config.distributeSurplus(me(), colApply, surplus), (i, span) -> {
            visibleCells.get(i).setColSpan(span);
        });

        for (int i = 0; i < colApply.length; i++) {
            visibleCells.get(i).setColSpan(colApply[i]);
        }
    }

    /**
     * Override the {@code this} method
     *
     * @return
     */
    @Override
    public abstract R me();

    /**
     * Add a node at the end of the row.
     *
     * @param node
     * @return
     */
    public R add(N node) {
        return add(-1, node);
    }

    /**
     * Add a node at given index.
     *
     * @param index
     * @param node
     * @return
     */
    public R add(int index, N node) {
        R me = me();
        finalAdd(index, Arrays.asList(node), config.getEnclosingNode(me), cells, 1);
        return me;
    }

    /**
     * Merge last cells in the row, with given component to hold the merged
     * cells.
     *
     * @param enclosing
     * @param lastCount
     * @return
     */
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

    /**
     * Merge cells with given <b>consecutive</b> indices, with given component
     * to hold the merged cells.
     *
     * @param enclosing
     * @param comps
     * @return
     */
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

            if (get.isMerged()) {
                throw new IllegalArgumentException("node num:" + num + " is part of a merged cell, can only merge free cells");
            }
        }

        ArrayList<Tuple<Integer, C>> before = new ArrayList<>();
        ArrayList<Tuple<Integer, C>> merge = new ArrayList<>();
        ArrayList<Tuple<Integer, C>> after = new ArrayList<>();

        ArrayList<C> newCells = new ArrayList<>();

        int beforeIndex = comps[0];
        int afterIndex = comps[comps.length - 1];

        For.elements().iterate(mainIterator(), (i, tup) -> {
            C cell = tup.g1;
            Integer colSpan = cell.getColSpan();
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

        For.elements().iterate(before, (i, tup) -> {
            C cell = tup.getG2();
            cell.setColSpan(tup.g1);
            newCells.add(cell);
        });

        int mergedColspan = merge.stream().mapToInt(m -> m.g1).sum();
        List<N> mergedNodes = merge.stream()
                .flatMap(m -> m.g2.getNodes().stream())
                .collect(Collectors.toList());
        merge.stream().map(m -> m.g2).forEach(cell -> {
            cell.setMerged(true);
        });

        C finalCell = this.finalAdd(-1, mergedNodes, enclosing.get(), newCells, mergedColspan);
        finalCell.setMerged(true);
        //insert after merged
        For.elements().iterate(after, (i, tup) -> {
            C cell = tup.getG2();
            cell.setColSpan(tup.g1);
            newCells.add(cell);
        });

        this.cells = newCells;

        return me();

    }

    /**
     * System method, usually don't use this.
     *
     * @param index index at which to add the cell.
     * @param nodes nodes to add (usually just one)
     * @param enclosing node to enclose the given nodes, can be null.
     * @param cellArray the array to add given cell.
     * @param colSpan
     * @return
     */
    protected C finalAdd(int index, List<N> nodes, N enclosing, List<C> cellArray, int colSpan) {

        C cell = config.createCell(nodes, enclosing, me());
        if (index < 0) {
            cellArray.add(cell);
        } else {
            cellArray.add(index, cell);
        }
        cell.setColSpan(colSpan);

        return cell;

    }

    /**
     * Change the colspan ratio of the cells. Must specify colspan for every
     * cell in the row.
     *
     * @param spans
     * @return
     */
    public R withPreferedColspan(Integer... spans) {
        R me = me();
        addOnDisplayAndRunIfDone(() -> {
            For.elements().iterate(spans, (i, spa) -> {
                C cell = this.getCell(i);
                cell.setColSpan(spa);

            });
            update();
        });

        return me;
    }

    /**
     * Gets a count of the nodes. Can be bigger than the cell count, because
     * cell can hold multiple nodes.
     *
     * @return
     */
    public int getNodeCount() {
        return cells.stream().mapToInt(m -> m.getNodes().size()).sum();
    }

    /**
     * System method, usually don't use this. Iterator of each tuple with cell
     * and a node. Each cell can hold multiple nodes.
     *
     * @return
     */
    protected ReadOnlyIterator<Tuple<C, N>> mainIterator() {
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
                return getCurrentCell().getNodes().get(compIndex);
            }

            @Override
            public Tuple<C, N> getCurrent() {
                return totalIndex >= 0 ? Tuples.create(getCurrentCell(), getCurrentComp()) : null;
            }

            @Override
            public int getCurrentIndex() {
                return totalIndex;
            }

            @Override
            public boolean hasNext() {
                return totalIndex + 1 < total;
            }

            private int currentCellChildrenSize() {
                return getCurrentCell().getNodes().size();
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

    /**
     * Gets every node in this row, can change the nodes directly, but not the
     * order.
     *
     * @return
     */
    public List<N> getNodes() {
        return mainIterator().map(m -> m.g2).toArrayList();
    }

    /**
     * Supplier that returns a cell, that satisfy the given node-index and node
     * predicate.
     *
     * @param pred
     * @return
     */
    public Supplier<C> getCellSupplier(Predicate<Tuple<Integer, N>> pred) {
        return () -> {
            return For.elements().find(
                    mainIterator(), (i, tuple) -> pred.test(Tuples.create(i, tuple.g2))
            ).map(m -> m.val.g1).orElse(null);
        };
    }

    /**
     * Supplier that returns a node, at given node-index
     *
     * @param i
     * @return
     */
    public Supplier<N> getNodeSupplier(Integer i) {
        return () -> {
            return For.elements().find(
                    mainIterator(), (j, tuple) -> Objects.equals(i, j)
            ).map(m -> m.val.g2).orElse(null);
        };
    }

    /**
     * Supplier that returns a node, that satisfy the given node predicate.
     *
     * @param pred
     * @return
     */
    public Supplier<N> getNodeSupplier(Predicate<N> pred) {

        return () -> {
            return For.elements().find(
                    mainIterator(), (j, tuple) -> pred.test(tuple.g2)
            ).map(m -> m.val.g2).orElse(null);
        };
    }

    /**
     * Returns a node, that satisfy the given node predicate.
     *
     * @param pred
     * @return
     */
    public <T extends N> T getNode(Predicate<N> pred) {
        return F.cast(getNodeSupplier(pred).get());
    }

    /**
     * Supplier that returns a node, that satisfy the given node-index.
     *
     * @param i
     * @return
     */
    public <T extends N> T getNode(Integer i) {
        return F.cast(getNodeSupplier(i).get());
    }

    /**
     * calls display(true)
     *
     * @return
     */
    public R display() {
        return display(true);
    }

    /**
     * Does UPDATES_ON_DISPLAY, UPDATES_ON_REFRESH and conditional
     * UPDATES_RENDER
     *
     * @param render wether to also render
     * @return
     */
    public R display(boolean render) {

        R me = me();
        if (displayed) {
            return me;
        }
        displayed = true;
        Checked.checkedRun(() -> {
            update(UPDATES_ON_DISPLAY);
        }).ifPresent(NestedException::nestedThrow);

        if (render) {
            render();
        }
        return me;

    }

    /**
     * Add a custom tag for this row, to mark for something to differentiate on.
     *
     * @param tag
     * @return
     */
    public R withTag(String... tag) {
        if (tags == null) {
            tags = new HashSet<>();
        }
        for (String t : tag) {
            this.tags.add(t);
        }
        return me();
    }

    /**
     * Check if row has been marked with a tag.
     *
     * @param tag
     * @return
     */
    public boolean hasTag(String tag) {
        if (tags == null) {
            return false;
        }
        return tags.contains(tag);
    }

    /**
     * Set display to false, and remove every cell.
     *
     * @return
     */
    public R clear() {
        displayed = false;
        cells.clear();
        return me();
    }

    /**
     * If row is displayed, execute this decorator instantly with given node
     * index, else just decorate it when it becomes displayed.
     *
     * @param index
     * @param cons
     * @return
     */
    public R withNodeDecorator(int index, BiConsumer<R, N>... cons) {
        return addOnDisplayAndRunIfDone(() -> {
            N node = getNode(index);
            R me = me();
            for (BiConsumer<R, N> con : cons) {
                con.accept(me, node);
            }
        });
    }

    /**
     * If row is displayed, execute this decorator instantly with given node
     * index, else just decorate it when it becomes displayed.
     *
     * @param index
     * @param cons
     * @return
     */
    public R withNodeDecorator(int index, Consumer<N>... cons) {
        return addOnDisplayAndRunIfDone(() -> {
            N node = getNode(index);
            for (Consumer<N> con : cons) {
                con.accept(node);
            }
        });
    }

    /**
     * Pass a decorator to modify this row.
     *
     * @param r
     * @return
     */
    public R withRowDecorator(Consumer<R> r) {
        r.accept(me());
        return me();
    }

    /**
     * Pass a decorator that only applies to different nodes in this row.
     *
     * @param <T>
     * @param type
     * @param cons
     * @return
     */
    public <T extends N> R withNodesOfTypeDo(Class<T> type, Consumer<T> cons) {
        Ins.InsCl<T> cl = Ins.of(type);
        getNodes().stream().filter(n -> cl.superClassOf(n)).map(m -> (T) m).forEach(cons);
        return me();
    }

    protected RecursiveRedirection redirection = new RecursiveRedirection()
            .setFirstRedirect(run -> { // only add an update if this is the first call. Update itself cannot add another update.
                if (isDisplayed()) {
                    run.run();
                }
                updates.get(UPDATES_ON_DISPLAY).addUpdate(run.asExecutedIn(1));
            });

    protected R addOnDisplayAndRunIfDone(Runnable run) {
        //protection form recursive calls, starts at 0.
        redirection.execute(run).ifPresent(NestedException::nestedThrow);
        return me();
    }
}
