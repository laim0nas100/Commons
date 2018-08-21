/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lt.lb.commons.Containers;

import java.util.*;
import java.util.function.Predicate;

/**
 *
 * @author Lemmin
 */
public class PrefillArrayMap2<T> implements Map<Integer, T> {

    private int size = 0;
    private int prefSize = 10;

    private T nullObject = null;
    private Object[] data = new Object[]{nullObject, nullObject, nullObject, nullObject, nullObject, nullObject, nullObject, nullObject, nullObject, nullObject};
    private Predicate nullTest = (Object t) -> {
        return Objects.equals(t, nullObject);
    };

    public PrefillArrayMap2(int preferedSize, Predicate nullTest, T nullObject) {
        this.nullObject = nullObject;
        this.nullTest = nullTest;
        this.prefSize = preferedSize;
        data = new Object[prefSize];
        Arrays.fill(data, nullObject);
    }

    public PrefillArrayMap2() {
        this(10, t -> t == null, null);
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    public boolean containsKey(Object key) {
        int k = (int) key;
        return (data.length > k) && !isNull(data[k]);
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
            return (T) data[k];
        }
        return nullObject;
    }

    public void grow(int to) {

        int newCap = (int) Math.max(Math.ceil(data.length + data.length * 0.2), to);
        data = Arrays.copyOf(data, newCap);

    }

    @Override
    public T put(Integer key, T value) {
        int k = key;
        if (data.length > k) {

            T item = (T) data[k];
            boolean valNull = isNull(value);
            boolean itemNull = isNull(item);
            if (itemNull && !valNull) {
                size++;
            } else if (!itemNull && valNull) {
                size--;
            }
            data[k] = value;
            return item;
        } else {
            grow(key);
            return put(key, value);
        }

    }

    private boolean isNull(Object ob) {
        return this.nullTest.test(ob);
    }

//    public void overWrite(Integer k, T val) {
//        if (data.length > k) {
//            if (data[k] == nullObject) {
//                size++;
//            }
//            data[k] = val;
//        } else {
//            grow(k);
//            overWrite(k, val);
//        }
//    }
    public T[] extractInterval(Integer from, Integer to) {
        return Arrays.copyOfRange((T[]) data, from, to);
    }

    public void overWriteRange(Integer from, Integer to, T val) {
        if (data.length < to) {
            if (val != nullObject) {
                size += to - data.length;
            }
            grow(to);
        }
        for (; from < to; from++) {
            data[from] = val;
        }
    }

    @Override
    public T remove(Object key) {
        int k = (int) key;
        if (data.length > k) {
            if (!isNull(data[k])) {
                size--;
            }
            T item = (T) data[k];
            data[k] = nullObject;
            return item;
        }
        return nullObject;
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
        int numNew = 0;
        for (Object ob : a) {
            if (!isNull(ob)) {
                numNew++;
            }
        }
        grow(size + growBy);
        int numMoved = size - index;
        if (numMoved > 0) {
//            Log.print("Array copy ", size, data.length, index, data, index + growBy, numMoved);
            System.arraycopy(data, index, data, index + growBy, numMoved);
        }

        System.arraycopy(a, 0, data, index, growBy);

        size += numNew;
    }

    @Override
    public void clear() {
        size = 0;
        data = new Object[prefSize];
        Arrays.fill(data, nullObject);
    }

    @Override
    public Set<Integer> keySet() {
        Set<Integer> set = new HashSet<>();
        for (int i = 0; i < data.length; i++) {
            if (data[i] != nullObject) {
                set.add(i);
            }
        }
        return set;
    }

    @Override
    public Collection<T> values() {
        ArrayList<T> values = new ArrayList<>(size);
        for (int i = 0; i < data.length; i++) {
            if (data[i] != nullObject) {
                values.add((T) data[i]);
            }
        }

        return values;

    }

    @Override
    public Set<Entry<Integer, T>> entrySet() {
        Set<Entry<Integer, T>> set = new HashSet<>();
        PrefillArrayMap2 me = this;

        for (int i = 0; i < data.length; i++) {

            if (!isNull(data[i])) {
                final Integer key = i;
                final T value = this.get(key);
                Entry<Integer, T> en = new Entry() {
                    @Override
                    public Object getKey() {
                        return key;
                    }

                    @Override
                    public Object getValue() {
                        return value;
                    }

                    @Override
                    public Object setValue(Object val) {
                        return me.put(key, val);
                    }

                    @Override
                    public String toString() {
                        return this.getKey() + " = " + this.getValue();
                    }
                };
                set.add(en);
            }

        }
        return set;
    }

}
