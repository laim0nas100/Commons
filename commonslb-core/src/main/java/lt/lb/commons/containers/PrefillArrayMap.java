/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lt.lb.commons.containers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import lt.lb.commons.misc.F;

/**
 *
 * @author Laimonas-Beniusis-PC
 */
public class PrefillArrayMap<T> implements Map<Integer, T> {

    private Map<Integer, T> positive;
    private Map<Integer, T> negative;
    private Tuple<Boolean, T> nullCase = new Tuple<>(false, null);

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
        F.iterate(m, (k, v) -> {
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
        if(this.nullCase.g1){
            set.add(null);
        }
        set.addAll(positive.keySet());
        F.iterate(negative.keySet(),(i,val)->{
           set.add(-val);
        });
        return set;
        
    }

    @Override
    public Collection<T> values() {
        ArrayList<T> values = new ArrayList<>(this.size());
        if(this.nullCase.g1){
            values.add(nullCase.g2);
        }
        values.addAll(negative.values());
        values.addAll(positive.values());
        return values;
    }

    @Override
    public Set<Entry<Integer, T>> entrySet() {
        HashSet<Entry<Integer,T>> set = new HashSet<>();
        if(this.nullCase.g1){
            final T nullVal = nullCase.g2;
            set.add(new Map.Entry<Integer,T>() {
                @Override
                public Integer getKey() {
                    return null;
                }

                @Override
                public T getValue() {
                    return nullVal;
                }

                @Override
                public T setValue(T value) {
                    nullCase.g1 = true;
                    T old = nullCase.g2;
                    nullCase.g2 = value;
                    return old;
                }
            });
            
        }
        F.iterate(negative.entrySet(), (i,entry)->{
            set.add(new Map.Entry<Integer,T>() {
                @Override
                public Integer getKey() {
                    return entry.getKey()*(-1);
                }

                @Override
                public T getValue() {
                    return entry.getValue();
                }

                @Override
                public T setValue(T value) {
                    return entry.setValue(value);
                }
            });
        });
        
        
        set.addAll(positive.entrySet());
        
        return set;
        
    }

}
