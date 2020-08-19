package lt.lb.commons.rows;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lt.lb.commons.F;
import lt.lb.commons.containers.caching.LazyDependantValue;

/**
 *
 * @author laim0nas100
 */
public abstract class Drows<R extends Drow, L, DR extends Drows, U extends Updates> implements UpdateAware<U, DR> {

    protected Map<String, U> updates = new HashMap<>();
    protected Map<String, R> rowMap = new HashMap<>();
    protected List<String> keyOrder = new ArrayList<>();
    protected final String composableKey;
    protected DrowsConf<DR, R, U> conf;
    protected Optional<DR> parentRows = Optional.empty();

    protected Map<String, DR> composable = new HashMap<>();

    protected LazyDependantValue<Map<String, Integer>> rowAndComposedKeyOrder = new LazyDependantValue<>(() -> {
        HashMap<String, Integer> indexMap = new HashMap<>();
        for (R row : rowMap.values()) {
            indexMap.put(row.getKey(), this.getRowIndex(row.getKey()));
        }
        for (DR rows : composable.values()) {
            indexMap.put(rows.getComposableKey(), this.getRowIndex(rows.getComposableKey()));
        }
        return indexMap;
    });
    protected LazyDependantValue<List<R>> rowsInOrder = rowAndComposedKeyOrder.map(m -> {
        List<R> collect = rowMap.values().stream().collect(Collectors.toList());
        Comparator<R> ofValue = Comparator.comparing(r -> m.getOrDefault(r.getKey(), -1));
        Collections.sort(collect, ofValue);
        return collect;
    });

    protected LazyDependantValue<List> rowsAndComposedInOrder = rowAndComposedKeyOrder.map(m -> {

        Map<Integer, List> composed = new HashMap<>();

        F.iterate(this.composable, (key, rows) -> {
            int indexKey = Math.max(m.getOrDefault(key, 10000), 0);
            composed.computeIfAbsent(indexKey, i -> new LinkedList<>()).add(rows);
        });

        F.iterate(rowsInOrder.get(), (index, row) -> {
            String key = row.getKey();
            int indexKey = Math.max(m.getOrDefault(key, 10000), 0);
            composed.computeIfAbsent(indexKey, i -> new LinkedList<>()).add(row);
        });

        Stream<Object> flatMap = composed.entrySet().stream().sorted(Comparator.comparing(v -> v.getKey()))
                .map(entry -> entry.getValue())
                .flatMap(list -> list.stream());

        List collect = flatMap.collect(Collectors.toList());
        return collect;
    });

    protected LazyDependantValue<List<R>> nestedRowsInOrder = rowsAndComposedInOrder.map(list -> {
        List<R> rows = new ArrayList<>();
        for (Object rr : list) {
            if (rr instanceof Drow) {
                rows.add(F.cast(rr));
            } else if (rr instanceof Drows) {
                Drows drows = F.cast(rr);
                rows.addAll(drows.getRowsInOrderNested());
            }
        }
        return rows;
    });

    protected LazyDependantValue<Map<String, Integer>> visibleRowsOrder = nestedRowsInOrder.map(list -> {
        Map<String, Integer> map = new HashMap<>();
        int index = 0;
        for (R row : list) {
            if (row.isRendable()) {
                map.put(row.getKey(), index);
                index++;
            }
        }

        return map;
    });

    @Override
    public Map<String, U> getUpdateMap() {
        return updates;
    }

    @Override
    public DrowsConf<DR, R, U> getConfig() {
        return this.conf;
    }

    public Drows(String key, DrowsConf<DR, R, U> conf) {
        composableKey = key;
        this.conf = conf;
        this.conf.configureUpdates(updates, me());

    }

    @Override
    public DR initUpdates() {
        UpdateAware.super.initUpdates();
        for (String name : defaultUpdateNames()) {
            this.withUpdate(name, 0, () -> {
                this.updateInOrder(name);
            });
        }

        this.withUpdateVisible(r -> {
            this.visibleRowsOrder.invalidate();
        });

        this.withUpdateRefresh(r -> {
            this.rowAndComposedKeyOrder.invalidate();
        });
        return me();
    }

    @Override
    public abstract DR me();

    public String getComposableKey() {
        return composableKey;
    }

    public boolean isEmpty() {
        return rowMap.isEmpty() && composable.isEmpty();
    }

    public Optional<DR> getLastParentRows() {
        Optional<DR> parent = this.getParentRows();

        Optional<DR> nextParent = parent;
        while (nextParent.isPresent()) {
            nextParent = parent.flatMap(m -> m.getParentRows());
            if (nextParent.isPresent()) {
                parent = nextParent;
            }
        }
        return parent;
    }

    public DR getLastParentOrMe() {
        return getLastParentRows().orElse(me());
    }

    public Optional<DR> getParentRows() {
        return parentRows;
    }

    public List<R> getRowsInOrderNested() {
        return nestedRowsInOrder.get();
    }

    public List<R> getRows() {
        return rowMap.values().stream().collect(Collectors.toList());
    }

    private void putKeyAt(Integer index, String key) {
        if (index >= 0) {
            keyOrder.add(index, key);
        } else {
            keyOrder.add(key);
        }
    }

    private void removeKey(String key) {
        keyOrder.remove(key);
    }

    public void removeIfContainsRow(String key) {
        if (rowMap.containsKey(key)) {
            removeRow(key);
        }
    }

    public void removeRow(String key) {
        if (!rowMap.containsKey(key)) {
            throw new IllegalArgumentException("Row with key:" + key + " is not present");
        }
        R remove = rowMap.remove(key);
        removeKey(key);
        rowAndComposedKeyOrder.invalidate();// manual trigger of update
        conf.removeRowDecorate(me(), remove);
    }

    public void removeAll() {

        composable.values().stream().collect(Collectors.toList()).forEach(composed -> {
            removeComposedRows(composed);
        });

        composable.clear();

        rowMap.values().stream().collect(Collectors.toList()).forEach(row -> {
            removeRow(row.getKey());
        });
        // in case we have some updaters configured
        rowMap.clear();
        keyOrder.clear();
        rowAndComposedKeyOrder.invalidate();
    }

    public void addRow(Integer index, R row) {
        if (rowMap.containsKey(row.getKey())) {
            throw new IllegalArgumentException("Row with key:" + row.getKey() + " is allready present");
        }

        rowMap.put(row.getKey(), row);

        putKeyAt(index, row.getKey());
        rowAndComposedKeyOrder.invalidate();// manual trigger of update
        conf.addRowDecorate(me(), row);

    }

    public void addRowAfter(String key, R row) {
        Integer index = rowAndComposedKeyOrder.get().getOrDefault(key, -10);
        addRow(index + 1, row);
    }

    public void addFirst(R row) {
        this.addRow(0, row);
    }

    public void addRow(R row) {
        this.addRow(-1, row);
    }

    public Optional<R> getLastRow() {
        return this.getRowsInOrder().stream().reduce((first, second) -> second);
    }

    public void composeRows(Integer index, DR rows) {
        String key = rows.getComposableKey();
        if (composable.containsKey(key) || rowMap.containsKey(key)) {
            throw new IllegalArgumentException(key + " is occupied");
        }

        if (rows.parentRows.isPresent()) {
            throw new IllegalArgumentException(key + " is allready being composed");
        }
        rows.parentRows = Optional.of(me());
        putKeyAt(index, key);
        me().composable.put(key, rows);
        me().bindDefaultUpdates(rows);
        rows.bindDefaultUpdates(me());

        rowAndComposedKeyOrder.invalidate();
        this.conf.composeDecorate(me(), rows);

//        return newRow;
    }

    public void removeComposedRows(DR rows) {
        if (!composable.containsKey(rows.getComposableKey())) {
            throw new IllegalArgumentException(rows.getComposableKey() + " is not found");
        }
        if (rows.parentRows.isEmpty()) {
            throw new IllegalArgumentException(rows.getComposableKey() + " is not allready being composed");
        }

        DR get = this.composable.get(rows.getComposableKey());
        if (get != rows) {
            throw new IllegalArgumentException("Composed rows reference missmatch");
        }
        rows.parentRows = Optional.empty();
        this.composable.remove(rows.getComposableKey());
        removeKey(rows.composableKey);
        
        me().unbindDefaultUpdates(rows);
        rows.unbindDefaultUpdates(me());
        rowAndComposedKeyOrder.invalidate();// manual trigger of update
        this.conf.uncomposeDecorate(me(), rows);
    }

    public void composeRowsLast(DR rows) {
        composeRows(-1, rows);
    }

    public Integer getRowIndex(String key) {
        return keyOrder.indexOf(key);
    }

    public Integer getVisibleRowIndex(String key) {
        return visibleRowsOrder.get().getOrDefault(key, -1);
    }

    public void renderEverything() {
        this.update(BasicUpdates.UPDATES_ON_RENDER);
    }

    public void invalidateRows() {
        this.update(BasicUpdates.UPDATES_ON_REFRESH);
    }

    public void invalidateVisibility() {
        this.update(BasicUpdates.UPDATES_ON_VISIBLE);
    }

    public Optional<R> getRowIf(String key, Predicate<R> comp) {
        return Optional.ofNullable(rowMap.getOrDefault(key, null)).filter(comp);
    }

    public Optional<R> getRow(String key) {
        return getRowIf(key, c -> true);
    }

    public R getOrCreate(String key) {
        Optional<R> row = getRow(key);
        if (row.isPresent()) {
            return row.get();
        } else {
            R newRow = conf.newRow(me(), key);
            addRow(newRow);
            return newRow;
        }
    }

    public R getNewAfter(String key) {
        R newRow = conf.newRow(me());
        Integer rowIndex = this.getRowIndex(key);
        this.addRow(rowIndex + 1, newRow);
        return newRow;
    }

    public R getNew() {
        R newRow = conf.newRow(me());
        addRow(newRow);
        return newRow;
    }

    public List<R> getRowsInOrder() {
        return rowsInOrder.get();
    }

    @Override
    public String toString() {
        return "DynamicRows{" + "composableKey=" + composableKey + '}';
    }

    public void updateInOrderNested(String type) {
        this.doInOrderNested(
                rows -> rows.update(type),
                row -> row.update(type)
        );
    }
    
    public void updateInOrder(String type) {
        this.doInOrder(row ->{
            row.update(type);
        });
    }
    
    public void doInOrder(Consumer<R> rowCons){
        for (R row : this.getRowsInOrder()) {
            rowCons.accept(row);
        }
    }

    public void doInOrderNested(Consumer<R> rowCons) {
        for (R row : this.getRowsInOrderNested()) {
            rowCons.accept(row);
        }
    }

    public void doInOrderNested(Consumer<DR> rowsCons, Consumer<R> rowCons) {
        for (Object ob : this.rowsAndComposedInOrder.get()) {
            if (ob instanceof Drow) {
                rowCons.accept(F.cast(ob));
            } else if (ob instanceof Drows) {
                rowsCons.accept(F.cast(ob));
            } else {
                throw new IllegalStateException("Found unrecognized object of" + ob);
            }
        }
    }

}
