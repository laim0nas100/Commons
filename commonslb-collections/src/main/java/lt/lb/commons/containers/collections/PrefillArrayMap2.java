package lt.lb.commons.containers.collections;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lt.lb.commons.ArrayOp;
import lt.lb.commons.F;
import lt.lb.commons.containers.tuples.Tuple;

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
    
    private Tuple<Boolean, T> nullObject = new Tuple<>(false, null);
    
    private Object[] data = ArrayOp.replicate(10, nullObject);
    
    public PrefillArrayMap2(int preferedSize) {
        this.prefSize = preferedSize;
        data = new Object[prefSize];
        Arrays.fill(data, nullObject);
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
    
    private Tuple<Boolean, T> unwrap(Object ob) {
        return F.cast(ob);
    }
    
    @Override
    public boolean containsKey(Object key) {
        int k = (int) key;
        return (data.length > k) && !isAbsent(unwrap(data[k]));
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
            Tuple<Boolean, T> item = unwrap(data[k]);
            return item.g2;
        }
        return null;
    }
    
    public void grow(int to) {
        int oldCap = data.length;
        int newCap = (int) Math.max(Math.ceil(oldCap * 1.1), to); // increase by 10%

        data = Arrays.copyOf(data, newCap);
        Arrays.fill(data, oldCap, newCap, this.nullObject);
        
    }
    
    private Tuple<Boolean, T> wrap(T value) {
        return new Tuple<>(true, value);
    }
    
    @Override
    public T put(Integer key, T val) {
        
        int k = key;
        if (data.length > k) {
            Tuple<Boolean, T> value = wrap(val);
            Tuple<Boolean, T> item = unwrap(data[k]);
            boolean valNull = isAbsent(value);
            boolean itemNull = isAbsent(item);
            if (itemNull && !valNull) {
                size++;
            } else if (!itemNull && valNull) {
                size--;
            }
            data[k] = value;
            return item.g2;
        } else {
            grow(key);
            return put(key, val);
        }
        
    }
    
    private boolean isAbsent(Tuple<Boolean, T> ob) {
        return ob.g1 == false;
    }
    
    @Override
    public T remove(Object key) {
        int k = (int) key;
        if (data.length > k) {
            Tuple<Boolean, T> item = unwrap(data[k]);
            if (!isAbsent(item)) {
                size--;
            }
            data[k] = nullObject;
            return item.g2;
        }
        return null;
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
            if (!isAbsent(unwrap(ob))) {
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
            Tuple<Boolean, T> t = unwrap(data[i]);
            if (t.g1) {
                set.add(i);
            }
        }
        return set;
    }
    
    @Override
    public Collection<T> values() {
        return Stream.of(data)
                .map(m -> unwrap(m))
                .filter(t -> t.g1)
                .map(m -> m.g2)
                .collect(Collectors.toList());
        
    }
    
    @Override
    public Set<Entry<Integer, T>> entrySet() {
        Set<Entry<Integer, T>> set = new HashSet<>();
        PrefillArrayMap2 me = this;
        
        for (int i = 0; i < data.length; i++) {
            Tuple<Boolean, T> item = unwrap(data[i]);
            
            if (!isAbsent(item)) {
                set.add(MapEntries.byKey(me, i));
            }
            
        }
        return set;
    }
    
}
