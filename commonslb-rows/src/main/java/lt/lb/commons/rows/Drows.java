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
public abstract class Drows<R extends Drow, L, DR extends Drows> {

    protected Map<String, R> rowMap = new HashMap<>();
    protected List<String> keyOrder = new ArrayList<>();
    protected final String composableKey;
    protected DrowsConf<DR, R> conf;

    protected Map<String, DR> composable = new HashMap<>();

    protected LazyDependantValue<Map<String, Integer>> rowKeyOrder = new LazyDependantValue<>(() -> {
        HashMap<String, Integer> indexMap = new HashMap<>();
        for (R row : rowMap.values()) {
            indexMap.put(row.getKey(), this.getRowIndex(row.getKey()));
        }
        return indexMap;
    });
    protected LazyDependantValue<List<R>> rowsInOrder = rowKeyOrder.map(m -> {
        List<R> collect = rowMap.values().stream().collect(Collectors.toList());
        Comparator<R> ofValue = Comparator.comparing(r -> m.getOrDefault(r.getKey(), -1));
        Collections.sort(collect, ofValue);
        return collect;
    });

    protected LazyDependantValue<List> dynamicRowsAndRowsInOrder = rowsInOrder.map(m -> {

        Map<Integer, List> composed = new HashMap<>();

        F.iterate(this.composable, (key, rows) -> {
            int index = Math.max(rowKeyOrder.get().getOrDefault(key, 0), 0);
            composed.computeIfAbsent(index, i -> new LinkedList<>()).add(rows);
        });

        F.iterate(m, (key, row) -> {
            composed.computeIfAbsent(key, i -> new LinkedList<>()).add(row);
        });

        Stream<Object> flatMap = composed.entrySet().stream().sorted(Comparator.comparing(v -> v.getKey()))
                .map(entry -> entry.getValue())
                .flatMap(list -> list.stream());

        List collect = flatMap.collect(Collectors.toList());
        return collect;
    });

    protected LazyDependantValue<Map<String, Integer>> visibleRowsOrder = rowsInOrder.map(list -> {
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

    public Drows(String key, DrowsConf<DR, R> conf) {
        composableKey = key;
        this.conf = conf;
    }

    protected abstract DR me();

    public String getComposableKey() {
        return composableKey;
    }

    public boolean isEmpty() {
        return rowMap.isEmpty();
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
        rowKeyOrder.invalidate();// manual trigger of update
    }

    public void removeAll() {
        composable.clear();
        // in case we have some updaters configured
        rowMap.values().forEach(r -> r.setDeleted(true));
        rowMap.clear();
        keyOrder.clear();
        rowKeyOrder.invalidate();
    }

    public void addRow(Integer index, R row) {
        if (rowMap.containsKey(row.getKey())) {
            throw new IllegalArgumentException("Row with key:" + row.getKey() + " is allready present");
        }

        rowMap.put(row.getKey(), row);
        putKeyAt(index, row.getKey());
        rowKeyOrder.invalidate();// manual trigger of update

    }

    public void addRowAfter(String key, R row) {
        Integer index = rowKeyOrder.get().getOrDefault(key, -10);
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

    public List<Object> getDynamicRowsAndRowsInOrder() {
        List list = dynamicRowsAndRowsInOrder.get();
//        Log.printLines(list);
        return list;
    }

    public List<R> getDynamicRowsInOrderNested() {
        ArrayList<R> all = new ArrayList<>();
        this.getDynamicRowsAndRowsInOrder().forEach(r -> {
            if (r instanceof Drow) {
                all.add(F.cast(r));
            } else if (r instanceof Drows) {
                Drows dr = F.cast(r);
                all.addAll(dr.getDynamicRowsInOrderNested());
            }
        });
        return all;
    }

    public R composeRows(Integer index, DR rows) {

        if (composable.containsKey(rows.getComposableKey())) {
            throw new IllegalArgumentException(rows.getComposableKey() + " is occupied");
        }
        R newRow = conf.newRow(me(), rows.getComposableKey());
        this.addRow(index, newRow);
        this.composable.put(rows.getComposableKey(), rows);

        this.conf.composeDecorate(me(), newRow, rows);

        return newRow;
    }

    public void removeComposedRows(DR rows) {
        if (!composable.containsKey(rows.getComposableKey())) {
            throw new IllegalArgumentException(rows.getComposableKey() + " is not found");
        }

        DR get = this.composable.get(rows.getComposableKey());
        if (get != rows) {
            throw new IllegalArgumentException("Composed rows reference missmatch");
        }
        this.composable.remove(rows.getComposableKey());

        this.removeRow(rows.getComposableKey());

    }

    public R composeRowsLast(DR rows) {
        return this.composeRows(-1, rows);
    }

    public Integer getRowIndex(String key) {
        return keyOrder.indexOf(key);
    }

    public Integer getVisibleRowIndex(String key) {
        return visibleRowsOrder.get().getOrDefault(key, -1);
    }

    public void invalidateRows() {
        this.rowKeyOrder.invalidate();
    }
    
    public void invalidateVisibility(){
        this.visibleRowsOrder.invalidate();
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

    public void update(String type) {
        this.doInOrder(
                rows -> rows.update(type),
                row -> row.update(type)
        );
    }

    public void doInOrder(Consumer<DR> rowsCons, Consumer<R> rowCons) {
        for (Object ob : this.getDynamicRowsAndRowsInOrder()) {
            if (ob instanceof Drow) {
                rowCons.accept(F.cast(ob));
            } else if (ob instanceof Drows) {
                rowsCons.accept(F.cast(ob));
            } else {
                throw new IllegalStateException("Found unrecognized object of" + ob);
            }
        }
    }

    public void doInOrderRows(Consumer<R> rowCons) {
        this.doInOrder(r -> {
        }, rowCons);
    }

}
