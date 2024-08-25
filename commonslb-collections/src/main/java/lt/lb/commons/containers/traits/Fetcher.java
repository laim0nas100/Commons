package lt.lb.commons.containers.traits;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import lt.lb.commons.Nulls;
import lt.lb.commons.containers.collections.PrefillArrayList;

/**
 *
 * @author laim0nas100
 */
public interface Fetcher<K, V> {
    
    V get(K key);
    
    V getOrCreate(K key, Function<? super K, ? extends V> mappingFunction);
    
    int size();
    
    void clear();
    
    public static abstract class IntFetcher<K, V> implements Fetcher<K, V> {
        
        protected List<V> list = new PrefillArrayList<>();
        
        @Override
        public V get(K key) {
            return list.get(toIndex(key));
        }
        
        public abstract int toIndex(K val);
        
        @Override
        public V getOrCreate(K key, Function<? super K, ? extends V> mappingFunction) {
            int index = toIndex(key);
            V val = list.get(index);
            if (val == null) {
                val = mappingFunction.apply(key);
                list.set(index, val);
            }
            return val;
        }
        
        @Override
        public int size() {
            return list.size();
        }
        
        @Override
        public void clear() {
            list.clear();
        }
        
    }
    
    public static class ListFetcher<T extends ListTraits> extends Fetcher.IntFetcher<T, Fetcher> {
        
        @Override
        public int toIndex(T val) {
            return val.getIndexForTraits();
        }
        
    }
    
    public static class MapFetcher<K, T extends MapTraits<K>> extends Fetcher.AbstractMapFetcher<K,T, Fetcher> {
        
        public MapFetcher() {
        }

        @Override
        public K toKey(T key) {
            return key.getKeyForTraits();
        }
        
    }

    public static abstract class AbstractMapFetcher<RK, K, V> implements Fetcher<K, V> {
        
        private Map<RK, V> map;
        
        public AbstractMapFetcher() {
            this.map = new HashMap<>();
        }
        
        public abstract RK toKey(K key);
        
        @Override
        public V get(K key) {
            return map.get(toKey(key));
        }
        
        @Override
        public V getOrCreate(K key, Function<? super K, ? extends V> mappingFunction) {
            return map.computeIfAbsent(toKey(key), k -> mappingFunction.apply(key));
        }
        
        @Override
        public int size() {
            return map.size();
        }
        
        @Override
        public void clear() {
            map.clear();
        }
    }
    
    public static class SimpleMapFetcher<K, V> implements Fetcher<K, V> {
        
        private Map<K, V> map;
        
        public SimpleMapFetcher(Map<K, V> map) {
            this.map = Nulls.requireNonNull(map);
        }
        
        @Override
        public V get(K key) {
            return map.get(key);
        }
        
        @Override
        public V getOrCreate(K key, Function<? super K, ? extends V> mappingFunction) {
            return map.computeIfAbsent(key, mappingFunction);
        }
        
        @Override
        public int size() {
            return map.size();
        }
        
        @Override
        public void clear() {
            map.clear();
        }
    }
    
    public static <K, V> Fetcher<K, V> hashMap() {
        return new SimpleMapFetcher<>(new HashMap<>());
    }
    
}
