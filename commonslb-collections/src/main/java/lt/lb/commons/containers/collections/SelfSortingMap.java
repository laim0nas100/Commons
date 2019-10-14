package lt.lb.commons.containers.collections;

import java.util.*;
import lt.lb.commons.F;

/**
 *
 * @author laim0nas100 a map with a priority queue to keep order
 * @param <K> key
 * @param <V> value
 *
 */
public class SelfSortingMap<K, V> implements Map<K, V> {

    private PriorityQueue<K> list;
    private Map<K, V> map;

    /**
     *
     * Initialize as a simple map
     *
     * @param <K> key
     * @param <V> value
     * @param cmp Comparator to sort items in queue
     * @param map
     */
    public SelfSortingMap(Comparator<K> cmp, Map<K, V> map) {
        this.list = new PriorityQueue<>(cmp);
        this.map = map;
    }

    @Override
    public V put(K k, V v) {

        V prevValue = map.put(k, v);
        if (prevValue == null) {
            this.list.add(k);
        }
        return prevValue;
    }

    @Override
    public V putIfAbsent(K k, V v) {
        if (!this.containsKey(k)) {
            return this.put(k, v);
        } else {
            return this.get(k);
        }
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        F.iterate(m, (k, v) -> {
            this.put(k, v);
        });
    }

    /**
     * returns all values in the order that they were sorted
     *
     * @return
     */
    public ArrayList<K> getOrderedList() {
        return new ArrayList<>(this.list);
    }

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return map.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return map.containsValue(value);
    }

    @Override
    public V get(Object key) {
        return map.get(key);
    }

    @Override
    public V remove(Object key) {
        V remove1 = map.remove(key);
        list.remove(key);
        return remove1;
    }

    @Override
    public void clear() {
        list.clear();
        this.map.clear();
    }

    @Override
    public Set<K> keySet() {
        return map.keySet();
    }

    @Override
    public Collection<V> values() {
        return map.values();
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        HashSet<Entry<K, V>> entries = new HashSet<>();
        F.iterate(map, (k, v) -> {
            entries.add(MapEntries.byKey(map, k));
        });
        return entries;
    }

}
