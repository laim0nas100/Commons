package lt.lb.commons.containers.collections;

import java.util.*;
import lt.lb.commons.containers.tuples.Tuple;
import lt.lb.commons.iteration.Iter;

/**
 *
 * Map backed by array, supports all integers and null keys
 *
 * @author laim0nas100
 */
public class PrefillArrayMap<T> implements Map<Integer, T> {

    protected Map<Integer, T> positive;
    protected Map<Integer, T> negative;
    protected Tuple<Boolean, T> nullCase = new Tuple<>(false, null);

    public PrefillArrayMap() {
        this.positive = new PrefillArrayMap2<>();
        this.negative = new PrefillArrayMap2<>();
    }

    private Map<Integer, T> getMap(Integer val) {
        if (val >= 0) {
            return positive;
        } else {
            return negative;
        }
    }

    @Override
    public int size() {
        int adNull = nullCase.g1 ? 1 : 0;
        return adNull + positive.size() + negative.size();

    }

    @Override
    public boolean isEmpty() {
        return size() == 0;
    }

    @Override
    public boolean containsKey(Object key) {
        if (key == null && nullCase.g1) {
            return true;
        }
        if (key instanceof Integer) {
            Integer k = (Integer) key;
            return this.getMap(k).containsKey(Math.abs(k));
        }
        return false;
    }

    @Override
    public boolean containsValue(Object value) {
        if (nullCase.g1 && Objects.equals(value, nullCase.g1)) {
            return true;
        }
        return positive.containsValue(value) || negative.containsValue(value);
    }

    @Override
    public T get(Object key) {
        if (key == null && nullCase.g1) {
            return nullCase.g2;
        }
        if (key instanceof Integer) {
            Integer k = (Integer) key;
            return this.getMap(k).get(Math.abs(k));
        }
        return null;

    }

    @Override
    public T put(Integer key, T value) {
        if (key == null) {
            T old = nullCase.g2;
            nullCase.g1 = true;
            nullCase.g2 = value;
            return old;
        }
        return this.getMap(key).put(Math.abs(key), value);
    }

    @Override
    public T remove(Object key) {
        if (key == null) {
            T old = nullCase.g2;
            nullCase.g1 = false;
            nullCase.g2 = null;
            return old;
        }

        if (key instanceof Integer) {
            Integer k = (Integer) key;
            return this.getMap(k).remove(Math.abs(k));
        }
        return null;

    }

    @Override
    public void putAll(Map<? extends Integer, ? extends T> m) {
        Iter.iterate(m, (k, v) -> {
            this.put(k, v);
        });
    }

    @Override
    public void clear() {
        this.nullCase.g1 = false;
        this.nullCase.g2 = null;
        this.positive.clear();
        this.negative.clear();
    }

    @Override
    public Set<Integer> keySet() {
        HashSet<Integer> set = new HashSet<>();
        if (this.nullCase.g1) {
            set.add(null);
        }
        set.addAll(positive.keySet());
        Iter.iterate(negative.keySet(), (i, val) -> {
            set.add(-val);
        });
        return set;

    }

    @Override
    public Collection<T> values() {
        ArrayList<T> values = new ArrayList<>(this.size());
        if (this.nullCase.g1) {
            values.add(nullCase.g2);
        }
        values.addAll(negative.values());
        values.addAll(positive.values());
        return values;
    }

    @Override
    public Set<Entry<Integer, T>> entrySet() {
        HashSet<Entry<Integer, T>> set = new HashSet<>();
        if (this.nullCase.g1) {
            set.add(MapEntries.byKey(this, null));

        }
        Iter.iterate(negative.entrySet(), (i, entry) -> {
            set.add(MapEntries.byMappingKey(this, () -> entry.getKey() * (-1)));
        });
        Iter.iterate(positive.entrySet(), (i, entry) -> {
            set.add(MapEntries.byKey(this, i));
        });
        return set;

    }

}
