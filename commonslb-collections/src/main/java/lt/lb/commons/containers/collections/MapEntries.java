package lt.lb.commons.containers.collections;

import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

/**
 *
 * Static constructors for Map.Entry class
 *
 * @author laim0nas100
 */
public class MapEntries {

    public static abstract class AbstractEntry<K, V> implements Map.Entry<K, V> {

        @Override
        public boolean equals(Object other) {
            if (other == this) {
                return true;
            }
            if (other instanceof Map.Entry) {
                Map.Entry entry = (Map.Entry) other;
                return Objects.equals(getKey(), entry.getKey()) && Objects.equals(getValue(), entry.getValue());
            }
            return false;
        }

        @Override
        public int hashCode() {
            return Objects.hash(getKey());
        }

        @Override
        public String toString() {
            return getKey() + "=" + getValue();
        }

    }

    public static <K, V> Map.Entry<K, V> byKey(Map<K, V> map, K k) {
        return new AbstractEntry<K, V>() {
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
        return new AbstractEntry<K, V>() {
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
                throw new UnsupportedOperationException("Immutable");
            }
        };
    }
}
