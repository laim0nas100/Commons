package lt.lb.commons.containers.collections;

import java.util.AbstractCollection;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import lt.lb.commons.F;

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

    public static class DetachedMapEntry<K, V> extends AbstractEntry<K, V> {

        private K key;
        private V value;

        public K setKey(K key) {
            K old = this.key;
            this.key = key;
            return old;
        }

        @Override
        public K getKey() {
            return key;
        }

        @Override
        public V getValue() {
            return value;
        }

        @Override
        public V setValue(V value) {
            V old = this.value;
            this.value = value;
            return old;
        }

    }

    public static class MapEntrySet<K, V> extends AbstractSet<Map.Entry<K, V>> {

        protected final Map<K, V> map;
        protected Function<Integer, Optional<Map.Entry<K, V>>> entryGenerator;
        protected final boolean mutable;

        protected Collection<V> values;
        protected Set<K> keySet;

        public MapEntrySet(boolean mutable, Map<K, V> map, Function<Integer, Optional<Map.Entry<K, V>>> entryGenerator) {
            this.mutable = mutable;
            this.map = map;
            this.entryGenerator = entryGenerator;
        }

        @Override
        public Iterator<Map.Entry<K, V>> iterator() {
            return new Iterator<Map.Entry<K, V>>() {
                int i = 0;
                Optional<Map.Entry<K, V>> cached = null;
                Map.Entry<K, V> lastNext = null;

                @Override
                public boolean hasNext() {
                    if (cached == null) {
                        cached = entryGenerator.apply(i);
                        i = cached.isPresent() ? i + 1 : i;
                    }
                    return cached.isPresent();
                }

                @Override
                public Map.Entry<K, V> next() {
                    if (cached == null) {
                        cached = entryGenerator.apply(i);
                        i = cached.isPresent() ? i + 1 : i;
                    }
                    Map.Entry<K, V> get = cached.get();
                    lastNext = get;
                    cached = null;
                    return get;
                }

                @Override
                public void remove() {
                    if (!mutable) {
                        throw new UnsupportedOperationException("No remove for " + map.getClass().getSimpleName());
                    }
                    if (lastNext == null) {
                        throw new IllegalStateException("Iterator.next has not been called or removed was called already");
                    }
                    // remove ok
                    map.remove(lastNext.getKey());
                    lastNext = null;
                    i--;// go back
                }

            };

        }

        @Override
        public int size() {
            return map.size();
        }

        @Override
        public boolean remove(Object o) {
            if (!mutable) {
                throw new UnsupportedOperationException("Immutable map " + map.getClass().getSimpleName());
            }
            if (o instanceof Map.Entry<?, ?>) {
                Map.Entry<?, ?> entry = F.cast(o);
                return map.remove(entry.getKey(), entry.getValue());
            }
            return false;
        }

        @Override
        public void clear() {
            if (!mutable) {
                throw new UnsupportedOperationException("Immutable map " + map.getClass().getSimpleName());
            }
            map.clear();
        }

        public Collection<V> values() {
            Collection<V> vals = values;
            if (vals == null) {
                vals = new AbstractCollection<V>() {
                    @Override
                    public Iterator<V> iterator() {
                        return new ValueIterator();
                    }

                    @Override
                    public int size() {
                        return MapEntrySet.this.size();
                    }

                    @Override
                    public boolean isEmpty() {
                        return MapEntrySet.this.isEmpty();
                    }

                    @Override
                    public void clear() {
                        MapEntrySet.this.clear();
                    }

                    @Override
                    public boolean contains(Object v) {
                        return map.containsValue(v);
                    }
                };
                values = vals;
            }
            return vals;
        }

        public Set<K> keySet() {
            Set<K> ks = keySet;
            if (ks == null) {
                ks = new AbstractSet<K>() {
                    @Override
                    public Iterator<K> iterator() {
                        return new KeyIterator();
                    }

                    @Override
                    public int size() {
                        return MapEntrySet.this.size();
                    }

                    @Override
                    public boolean isEmpty() {
                        return MapEntrySet.this.isEmpty();
                    }

                    @Override
                    public void clear() {
                        MapEntrySet.this.clear();
                    }

                    @Override
                    public boolean contains(Object k) {
                        return map.containsKey(k);
                    }
                };
                keySet = ks;
            }
            return ks;
        }

        class KeyIterator implements Iterator<K> {

            private final Iterator<Map.Entry<K, V>> i = iterator();

            @Override
            public boolean hasNext() {
                return i.hasNext();
            }

            @Override
            public void remove() {
                i.remove();
            }

            @Override
            public K next() {
                return i.next().getKey();
            }
        }

        class ValueIterator implements Iterator<V> {

            private final Iterator<Map.Entry<K, V>> i = iterator();

            @Override
            public boolean hasNext() {
                return i.hasNext();
            }

            @Override
            public void remove() {
                i.remove();
            }

            @Override
            public V next() {
                return i.next().getValue();
            }
        }

    }
}
