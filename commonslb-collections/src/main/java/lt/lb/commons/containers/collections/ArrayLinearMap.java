package lt.lb.commons.containers.collections;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 *
 * @author laim0nas100
 */
public class ArrayLinearMap<K, V> implements Map<K, V>, Cloneable, Serializable {

    private static final Object[] EMPTY_DATA = new Object[0];
    protected Object[] data;

    public ArrayLinearMap() {
        this.data = EMPTY_DATA;
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
        int find = find(0, key);
        V oldValue = null;
        int index = -1;
        if (find >= 0) {// value replace
            index = find + 1;
            oldValue = (V) data[index];
            data[index] = value;

        } else {// new value
            index = data.length;
            Object[] newData = Arrays.copyOf(data, data.length + 2);
            newData[index] = key;
            newData[index + 1] = value;
            data = newData;
        }
        return oldValue;
    }

    @Override
    public V remove(Object key) {
        int keyIndex = find(0, key);
        if (keyIndex < 0) {//not found
            return null;
        } else {
            V oldValue = (V) data[keyIndex + 1];
            Object[] newData = new Object[data.length - 2];
            boolean found = false;
            for (int i = 0; i < data.length; i += 2) {
                if (i == keyIndex) {
                    found = true;
                    continue;
                }
                int index = found ? i - 2 : i;
                newData[index] = data[i];
                newData[index + 1] = data[i + 1];
            }
            data = newData;
            return oldValue;
        }
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        for (Entry<? extends K, ? extends V> entry : m.entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public void clear() {
        data = EMPTY_DATA;
    }

    private static Object[] extractKeys(boolean check, Object[] array) {
        if (check && Objects.requireNonNull(array).length % 2 != 0) {
            throw new IllegalArgumentException("Must be even size:" + array.length);
        }
        int size = array.length / 2;
        Object[] keys = new Object[size];
        for (int i = 0; i < size; i++) {
            keys[i] = array[i * 2];
        }
        if (check) {
            ImmutableLinearSet.assertDistinct(keys);
        }

        return keys;
    }

    @Override
    public Set<K> keySet() {
        return new ImmutableLinearSet<>(false, extractKeys(false, data));//was prechecked
    }

    @Override
    public Collection<V> values() {
        ArrayList<V> array = new ArrayList<>(size());
        for (int i = 0; i < size(); i++) {
            array.add((V) data[1 + (i * 2)]);
        }

        return Collections.unmodifiableList(array);
    }

    @Override
    public Set<Map.Entry<K, V>> entrySet() {
        Map.Entry[] entries = new Map.Entry[size()];
        for (int i = 0; i < size(); i++) {
            entries[i] = MapEntries.immutable(data[i * 2], data[1 + (i * 2)]);
        }
        return new ImmutableLinearSet<>(false, entries);//was prechecked
    }

    @Override
    public Object clone() {
        try {
            return (ArrayLinearMap<K, V>) super.clone();// array should be copied
        } catch (CloneNotSupportedException e) {
            // this shouldn't happen, since we are Cloneable
            throw new InternalError(e);
        }
    }

}
