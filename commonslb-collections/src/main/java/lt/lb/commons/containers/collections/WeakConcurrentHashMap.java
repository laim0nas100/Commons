package lt.lb.commons.containers.collections;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 *
 * adapted from original made by zhanhb
 *
 * @param <K>
 * @param <V>
 */
public class WeakConcurrentHashMap<K, V> extends AbstractMap<K, V>
        implements ConcurrentMap<K, V> {

    private final ConcurrentMap<Key<K>, V> map;
    private final ReferenceQueue<K> queue = new ReferenceQueue<>();
    private transient Set<Map.Entry<K, V>> es;

    protected final boolean identity;

    public WeakConcurrentHashMap(int initialCapacity, boolean useIdentity) {
        this.map = new ConcurrentHashMap<>(initialCapacity);
        this.identity = useIdentity;
    }

    public WeakConcurrentHashMap(boolean useIdentity) {
        this.map = new ConcurrentHashMap<>();
        this.identity = useIdentity;
    }

    public WeakConcurrentHashMap() {
        this(false);
    }

    public WeakConcurrentHashMap(int initialCapacity) {
        this(initialCapacity, false);
    }

    @Override
    public V get(Object key) {
        purgeKeys();
        return map.get(key(key));
    }

    @Override
    public V put(K key, V value) {
        purgeKeys();
        return map.put(keyQ(key), value);
    }

    @Override
    public int size() {
        purgeKeys();
        return map.size();
    }

    @SuppressWarnings({"NestedAssignment", "element-type-mismatch"})
    private void purgeKeys() {
        Reference<? extends K> reference;
        while ((reference = queue.poll()) != null) {
            map.remove(reference);
        }
    }

    @Override
    @SuppressWarnings("NestedAssignment")
    public Set<Map.Entry<K, V>> entrySet() {
        Set<Map.Entry<K, V>> entrySet;
        return ((entrySet = this.es) == null) ? es = new EntrySet() : entrySet;
    }

    @Override
    public V putIfAbsent(K key, V value) {
        purgeKeys();
        return map.putIfAbsent(keyQ(key), value);
    }

    @Override
    public V remove(Object key) {
        return map.remove(key(key));
    }

    @Override
    public boolean remove(Object key, Object value) {
        purgeKeys();
        return map.remove(key(key), value);
    }

    @Override
    public boolean replace(K key, V oldValue, V newValue) {
        purgeKeys();
        return map.replace(key(key), oldValue, newValue);
    }

    @Override
    public V replace(K key, V value) {
        purgeKeys();
        return map.replace(key(key), value);
    }

    @Override
    public boolean containsKey(Object key) {
        purgeKeys();
        return map.containsKey(key(key));
    }

    private <A> Key<A> key(A key) {
        return new Key<>(key, null, identity);
    }

    private Key<K> keyQ(K key) {
        return new Key<>(key, queue, identity);
    }

    @Override
    @SuppressWarnings("empty-statement")
    public void clear() {
        while (queue.poll() != null);
        map.clear();
    }

    @Override
    public boolean containsValue(Object value) {
        purgeKeys();
        return map.containsValue(value);
    }

    private static class Key<T> extends WeakReference<T> {

        private final int hash;
        private final boolean identity;

        Key(T t, ReferenceQueue<T> queue, boolean useIdentity) {
            super(Objects.requireNonNull(t, "Null keys not allowed"), queue);
            hash = useIdentity ? System.identityHashCode(t) : Objects.hashCode(t);
            identity = useIdentity;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (obj instanceof Key) {
                Key k = (Key) obj;
                return identity ? k.get() == get() : Objects.equals(k.get(), get());
            }
            return false;
        }

        @Override
        public int hashCode() {
            return hash;
        }

    }

    private class Iter implements Iterator<Map.Entry<K, V>> {

        private final Iterator<Map.Entry<Key<K>, V>> it;
        private Map.Entry<K, V> nextValue;

        Iter(Iterator<Map.Entry<Key<K>, V>> it) {
            this.it = it;
        }

        @Override
        public boolean hasNext() {
            if (nextValue != null) {
                return true;
            }
            while (it.hasNext()) {
                Map.Entry<Key<K>, V> entry = it.next();
                K key = entry.getKey().get();
                if (key != null) {
                    nextValue = new Entry(key, entry.getValue());
                    return true;
                } else {
                    it.remove();
                }
            }
            return false;
        }

        @Override
        public Map.Entry<K, V> next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            Map.Entry<K, V> entry = nextValue;
            nextValue = null;
            return entry;
        }

        @Override
        public void remove() {
            it.remove();
            nextValue = null;
        }

    }

    private class EntrySet extends AbstractSet<Map.Entry<K, V>> {

        @Override
        public Iterator<Map.Entry<K, V>> iterator() {
            return new Iter(map.entrySet().iterator());
        }

        @Override
        public int size() {
            return WeakConcurrentHashMap.this.size();
        }

        @Override
        public void clear() {
            WeakConcurrentHashMap.this.clear();
        }

        @Override
        @SuppressWarnings("element-type-mismatch")
        public boolean contains(Object o) {
            if (!(o instanceof Map.Entry)) {
                return false;
            }
            Map.Entry<?, ?> e = (Map.Entry<?, ?>) o;
            return WeakConcurrentHashMap.this.get(e.getKey()) == e.getValue();
        }

        @Override
        public boolean remove(Object o) {
            if (!(o instanceof Map.Entry)) {
                return false;
            }
            Map.Entry<?, ?> e = (Map.Entry<?, ?>) o;
            return WeakConcurrentHashMap.this.remove(e.getKey(), e.getValue());
        }
    }

    private class Entry extends AbstractMap.SimpleEntry<K, V> {

        private static final long serialVersionUID = 1L;

        Entry(K key, V value) {
            super(key, value);
        }

        @Override
        public V setValue(V value) {
            WeakConcurrentHashMap.this.put(getKey(), value);
            return super.setValue(value);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof Map.Entry) {
                Map.Entry<?, ?> e = (Map.Entry<?, ?>) obj;
                return getKey() == e.getKey() && getValue() == e.getValue();
            }
            return false;
        }

        @Override
        public int hashCode() {
            if (identity) {
                return System.identityHashCode(getKey())
                        ^ System.identityHashCode(getValue());
            } else {
                return Objects.hash(getKey())
                        ^ Objects.hash(getValue());
            }
        }
    }

}
