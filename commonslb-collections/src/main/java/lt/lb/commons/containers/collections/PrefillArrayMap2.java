package lt.lb.commons.containers.collections;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lt.lb.commons.ArrayOp;
import lt.lb.commons.F;

/**
 *
 *
 * Map backed by array, only positive. No hashing.
 *
 * @author laim0nas100
 */
class PrefillArrayMap2<T> implements Map<Integer, T> {

    private int size = 0;
    private int prefSize = 10;

    private Object[] data = ArrayOp.replicate(10, F.EMPTY_OBJECT);

    public PrefillArrayMap2(int preferedSize) {
        this.prefSize = preferedSize;
        data = new Object[prefSize];
        Arrays.fill(data, F.EMPTY_OBJECT);
    }

    public PrefillArrayMap2() {
        this(10);
    }

    /*
     * @inheritDoc
     */
    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    private T unwrap(Object ob) {
        if (ob == F.EMPTY_OBJECT) {
            return null;
        }
        return F.cast(ob);
    }

    @Override
    public boolean containsKey(Object key) {
        int k = (int) key;
        return (data.length > k) && data[k] != F.EMPTY_OBJECT;
    }

    @Override
    public boolean containsValue(Object value) {
        for (Object ob : data) {
            if (Objects.equals(ob, value)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public T get(Object key) {
        int k = (int) key;
        if (data.length > k) {
            return unwrap(data[k]);
        }
        return null;
    }

    public void grow(int to) {
        int oldCap = data.length;
        int newCap = (int) Math.max(Math.ceil(oldCap * 1.1), to); // increase by 10%

        data = Arrays.copyOf(data, newCap);
        Arrays.fill(data, oldCap, newCap, F.EMPTY_OBJECT);

    }

    @Override
    public T put(Integer key, T val) {

        int k = key;
        if (data.length > k) {
            Object prev = data[k];
            boolean valNull = val == F.EMPTY_OBJECT;
            boolean itemNull = prev == F.EMPTY_OBJECT;

            if (itemNull && !valNull) {
                size++;

            } else if (!itemNull && valNull) {
                size--;
            }
            if (itemNull) {
                prev = null;
            }
            data[k] = val;
            return (T) prev;

        } else {
            grow(key);
            return put(key, val);
        }

    }

    @Override
    public T remove(Object key) {
        return put((int) key, (T) F.EMPTY_OBJECT);
    }

    @Override
    public void putAll(Map<? extends Integer, ? extends T> m) {
        for (Entry<? extends Integer, ? extends T> entry : m.entrySet()) {
            this.put(entry.getKey(), entry.getValue());
        }
    }

    public void addAll(int index, Collection<? extends T> clctn) {
        Object[] a = clctn.toArray();
        int growBy = a.length;
        grow(size + growBy);
        int numMoved = size - index;
        if (numMoved > 0) {
//            Log.print("Array copy ", size, data.length, index, data, index + growBy, numMoved);
            System.arraycopy(data, index, data, index + growBy, numMoved);
        }

        System.arraycopy(a, 0, data, index, growBy);

        size += a.length;
    }

    @Override
    public void clear() {
        size = 0;
        data = new Object[prefSize];
        Arrays.fill(data, F.EMPTY_OBJECT);
    }

    @Override
    public Set<Integer> keySet() {
        Set<Integer> set = new HashSet<>();
        for (int i = 0; i < data.length; i++) {
            T t = unwrap(data[i]);
            if (t != null) {
                set.add(i);
            }
        }
        return set;
    }

    @Override
    public Collection<T> values() {
        return Stream.of(data)
                .filter(t -> t != F.EMPTY_OBJECT)
                .map(m -> unwrap(m))
                .collect(Collectors.toList());

    }

    @Override
    public Set<Entry<Integer, T>> entrySet() {
        Set<Entry<Integer, T>> set = new HashSet<>();
        PrefillArrayMap2 me = this;

        for (int i = 0; i < data.length; i++) {
            if(data[i] == F.EMPTY_OBJECT){
                continue;
            }
            set.add(MapEntries.byKey(me, i));

        }
        return set;
    }

}
