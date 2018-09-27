/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lt.lb.commons.containers.caching;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 *
 * @author laim0nas100
 * @param <K> key type
 * @param <V> value type
 */
public class CachedMap<K, V> implements Map<K, V> {

    private static final Object NULLKEY = "The key was null";
    public boolean enabled = true;
    private Map<Object, LocalCache<K, V>> innerMap = new ConcurrentHashMap<>();
    private Localizer localizer;

    public static class LocalCache<K, V> extends ConcurrentHashMap<K, V> {

        public Object stamp;
        public AtomicLong lastRead;
        public AtomicLong lastWrite;

        private <K, V> LocalCache(Object s) {
            stamp = s;
            lastRead = new AtomicLong(-1);
            lastWrite = new AtomicLong(-1);
        }

        public void updateRead() {
            this.lastRead.set(CachedValue.millisAtDefaultZone());
        }

        public void updateWrite() {
            this.lastWrite.set(CachedValue.millisAtDefaultZone());
        }
    }

    public static interface Localizer {

        public Object getLocalKey();
    }

    protected LocalCache<K, V> getCache() {
        Object key = localizer.getLocalKey();
        if (key == null) {
            // return throw-away cache
            return new LocalCache<>(NULLKEY);
        }
        if (innerMap.containsKey(key)) {
            return innerMap.get(key);
        } else {
            LocalCache<K, V> cache = new LocalCache<>(key);
            innerMap.put(key, cache);
            return cache;
        }
    }

    public <K, V> CachedMap(Localizer loc) {
        this.localizer = loc;
    }

    public Object getStamp() {
        return getCache().stamp;
    }

    //standard methods
    @Override
    public int size() {
        if (!enabled) {
            return -1;
        }
        return getCache().size();
    }

    @Override
    public boolean isEmpty() {
        if (!enabled) {
            return true;
        }
        return getCache().isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        if (!enabled) {
            return false;
        }

        return getCache().containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        if (!enabled) {
            return false;
        }
        LocalCache<K, V> cache = getCache();
        cache.updateRead();
        return cache.containsValue(value);
    }

    @Override
    public V get(Object key) {
        if (!enabled) {
            return null;
        }
        LocalCache<K, V> cache = getCache();
        cache.updateRead();
        return cache.get(key);
    }

    @Override
    public V put(K key, V value) {
        if (!enabled) {
            return value;
        }
        LocalCache<K, V> cache = getCache();
        cache.updateWrite();
        return cache.put(key, value);
    }

    @Override
    public V remove(Object key) {
        if (!enabled) {
            return null;
        }
        LocalCache<K, V> cache = getCache();
        cache.updateWrite();
        return cache.remove(key);
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        if (!enabled) {
            return;
        }
        LocalCache<K, V> cache = getCache();
        cache.updateWrite();
        cache.putAll(m);
    }

    @Override
    public void clear() {
        if (!enabled) {
            return;
        }
        LocalCache<K, V> cache = getCache();
        cache.updateWrite();
        cache.clear();
    }

    @Override
    public Set<K> keySet() {
        if (!enabled) {
            return null;
        }
        LocalCache<K, V> cache = getCache();
        cache.updateRead();
        return getCache().keySet();
    }

    @Override
    public Collection<V> values() {
        if (!enabled) {
            return null;
        }
        LocalCache<K, V> cache = getCache();
        cache.updateRead();
        return getCache().values();
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        if (!enabled) {
            return null;
        }
        LocalCache<K, V> cache = getCache();
        cache.updateRead();
        return getCache().entrySet();
    }

    public Long getLastRead() {
        return getCache().lastRead.get();
    }

    public Long getLastWrite() {
        return getCache().lastWrite.get();
    }

    public Collection<LocalCache<K, V>> getLocalizedCaches() {
        return this.innerMap.values();
    }

    public static <Key, Val> CachedMap<Key, Val> getSingleton(String name) {
        LocalCache<Key, Val> localCache = new LocalCache<>("singleton");
        CachedMap<Key, Val> map = new CachedMap<Key, Val>(() -> 0) {
            @Override
            protected LocalCache<Key, Val> getCache() {
                return localCache;
            }
        };
        map.innerMap.put(localCache.stamp, localCache);
        return map;
    }

}
