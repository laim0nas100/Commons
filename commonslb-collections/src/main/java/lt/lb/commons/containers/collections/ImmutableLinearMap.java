package lt.lb.commons.containers.collections;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Linear lookup immutable map, for small data;
 * @author laim0nas100
 */
public class ImmutableLinearMap<K, V> implements Map<K, V> {

    protected final Object[] data;

    public ImmutableLinearMap(boolean check, Object[] data) {
        extractKeys(check, data);
        this.data = data;
    }

    public ImmutableLinearMap(Object... data) {
        this(true, data);
    }

    @Override
    public int size() {
        return data.length / 2;
    }

    @Override
    public boolean isEmpty() {
        return data.length == 0;
    }

    private int find(int offset, Object value) {
        for (int i = 0; i < data.length; i += 2) {
            int index = i + offset;
            if (Objects.equals(value, data[index])) {
                return index;
            }
        }
        return -1;
    }

    @Override
    public boolean containsKey(Object key) {
        return find(0, key) >= 0;
    }

    @Override
    public boolean containsValue(Object value) {
        return find(1, value) >= 0;
    }

    @Override
    public V get(Object key) {
        int index = find(0, key);
        if (index >= 0) {
            return (V) data[index + 1];
        }
        return null;
    }

    @Override
    public V put(K key, V value) {
        throw new UnsupportedOperationException("ImmutableLinearMap");
    }

    @Override
    public V remove(Object key) {
        throw new UnsupportedOperationException("ImmutableLinearMap");
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        throw new UnsupportedOperationException("ImmutableLinearMap");
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException("ImmutableLinearMap");
    }

    private static Object[] extractKeys(boolean check, Object[] array) {
        if (check && Objects.requireNonNull(array).length % 2 != 0) {
            throw new IllegalArgumentException("Must be even size:" + array.length);
        }
        int size = array.length / 2;
        Object[] keys = new Object[size];
        for (int i = 0; i < size; i++) {
            Object k = array[i * 2];
            keys[i] = k;
            if (check && i > 0) {//at least 2
                for (int j = 0; j < i; j++) {
                    if (Objects.equals(k, keys[j])) {
                        throw new IllegalArgumentException("Duplicate key:" + k);
                    }
                }
            }
        }

        return keys;
    }

    private Set<K> keyset;

    @Override
    public Set<K> keySet() {
        if (keyset != null) {
            return keyset;
        }
        keyset = new ImmutableLinearSet<>(false, extractKeys(false, data));//was prechecked
        return keyset;
    }

    private List<V> values;

    @Override
    public Collection<V> values() {
        if (values != null) {
            return values;
        }
        ArrayList<V> array = new ArrayList<>(size());
        for (int i = 0; i < size(); i++) {
            array.add((V) data[1 + (i * 2)]);
        }

        values = Collections.unmodifiableList(array);
        return values;
    }

    private Set<Entry<K, V>> entrySet;

    @Override
    public Set<Entry<K, V>> entrySet() {
        if (entrySet != null) {
            return entrySet;
        }
        Entry[] entries = new Entry[size()];
        for (int i = 0; i < size(); i++) {
            entries[i] = MapEntries.immutable(data[i * 2], data[1 + (i * 2)]);
        }
        entrySet = new ImmutableLinearSet<>(false, entries);//was prechecked
        return entrySet;
    }

}
