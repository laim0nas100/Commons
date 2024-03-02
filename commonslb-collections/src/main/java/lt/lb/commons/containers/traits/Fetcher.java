package lt.lb.commons.containers.traits;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import lt.lb.commons.Nulls;

/**
 *
 * @author laim0nas100
 */
public interface Fetcher<K, V> {

    V get(K key);

    V getOrCreate(K key, Function<? super K, ? extends V> mappingFunction);

    int size();

    void clear();

    public static class MapFetcher<K, V> implements Fetcher<K, V> {

        private Map<K, V> map;

        public MapFetcher(Map<K, V> map) {
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
        return new MapFetcher<>(new HashMap<>());
    }

}
