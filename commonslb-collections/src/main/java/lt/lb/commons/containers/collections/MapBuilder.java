package lt.lb.commons.containers.collections;

import java.util.Map;
import java.util.Objects;

/**
 *
 * @author laim0nas100
 */
public class MapBuilder<K, V, M extends Map<K, V>> {

    protected M map;

    public MapBuilder(M map) {
        this.map = Objects.requireNonNull(map);
    }

    public MapBuilder<K, V, M> put(K key, V val) {
        getMap().put(key, val);
        return this;
    }

    public MapBuilder<K, V, M> putAll(Map<? extends K, ? extends V> otherMap) {
        Objects.requireNonNull(otherMap, "Provided map is null");
        getMap().putAll(otherMap);
        return this;
    }

    public MapBuilder<K, V, M> putIfAbsent(K key, V val) {
        getMap().putIfAbsent(key, val);
        return this;
    }

    public M getMap() {
        return map;
    }

}
