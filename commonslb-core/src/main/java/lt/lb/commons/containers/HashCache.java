/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lt.lb.commons.containers;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Policy;
import com.github.benmanes.caffeine.cache.stats.CacheStats;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;
import lt.lb.commons.misc.F;

/**
 *
 * @author Lemmin
 */
public class HashCache<K, V> implements Cache<K, V> {

    private HashMap<K, V> map = new HashMap<>();

    @Override
    public V getIfPresent(Object key) {
        return map.get(key);
    }

    @Override
    public V get(K key, Function<? super K, ? extends V> mappingFunction) {
        V get = this.getIfPresent(key);
        if (get == null) {
            V apply = mappingFunction.apply(key);
            this.put(key, apply);
            return apply;
        }
        return get;
    }

    @Override
    public Map<K, V> getAllPresent(Iterable<?> keys) {

        HashMap<K, V> copy = new HashMap<>();
        for (Object k : keys) {
            V get = getIfPresent(k);
            if (get != null) {
                copy.put(F.cast(k), get);
            }
        }
        return copy;
    }

    @Override
    public void put(K key, V value) {
        map.put(key, value);
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> map) {
        this.map.putAll(map);
    }

    @Override
    public void invalidate(Object key) {
        this.map.remove(key);
    }

    @Override
    public void invalidateAll(Iterable<?> keys) {
        for (Object k : keys) {
            this.invalidate(k);
        }
    }

    @Override
    public void invalidateAll() {
        this.map.clear();
    }

    @Override
    public long estimatedSize() {
        return this.map.size();
    }

    @Override
    public CacheStats stats() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ConcurrentMap<K, V> asMap() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void cleanUp() {
        map.clear();
    }

    @Override
    public Policy<K, V> policy() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
