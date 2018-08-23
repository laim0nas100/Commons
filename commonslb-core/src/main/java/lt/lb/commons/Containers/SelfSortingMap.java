/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lt.lb.commons.Containers;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

/**
 *
 * @author Lemmin Initialize as a simple map
 * @param <K> key
 * @param <V> value
 *
 */
public class SelfSortingMap<K, V> extends HashMap<K, V> {

    private PriorityQueue<V> list;

    /**
     *
     * Initialize as a simple map
     *
     * @param <K> key
     * @param <V> value
     * @param cmp Comparator to sort items in queue
     */
    public SelfSortingMap(Comparator<V> cmp) {
        this.list = new PriorityQueue<>(cmp);
    }

    @Override
    public V put(K k, V v) {

        V prevValue = super.put(k, v);
        if (prevValue != null) {
            this.list.remove(prevValue);
        }
        this.list.add(v);
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
    public void putAll(Map map) {
        throw new UnsupportedOperationException();
    }

    /**
     * returns all values in the order that they were sorted
     *
     * @return
     */
    public List<V> getOrderedList() {
        return new ArrayList<>(this.list);
    }

}
