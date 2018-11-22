/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lt.lb.commons.containers.collections;

import java.util.Map;
import java.util.function.Supplier;

/**
 *
 * Static constructors for Map.Entry class
 *
 * @author laim0nas100
 */
public class MapEntries {

    public static <K, V> Map.Entry<K, V> byKey(Map<K, V> map, K k) {
        return new Map.Entry<K, V>() {
            @Override
            public K getKey() {
                return k;
            }

            @Override
            public V getValue() {
                return map.get(k);
            }

            @Override
            public V setValue(V value) {
                return map.put(k, value);
            }
        };
    }

    public static <K, V> Map.Entry<K, V> byMappingKey(Map<K, V> map, Supplier<K> sup) {
        return byKey(map, sup.get());
    }

    public static <K, V> Map.Entry<K, V> immutable(K k, V v) {
        return new Map.Entry<K, V>() {
            @Override
            public K getKey() {
                return k;
            }

            @Override
            public V getValue() {
                return v;
            }

            @Override
            public V setValue(V value) {
                throw new UnsupportedOperationException("Not supported");
            }
        };
    }
}
