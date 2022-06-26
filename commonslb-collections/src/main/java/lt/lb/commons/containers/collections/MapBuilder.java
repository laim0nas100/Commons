package lt.lb.commons.containers.collections;

import java.util.List;
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
    
    public MapBuilder<K,V,M> putAll(List<K> keys, List<V> values){
        int size = keys.size();
        if(size != values.size()){
            throw new IllegalArgumentException("Keys size does not correspond to value size:"+size+" to "+values.size());
        }
        for(int i = 0; i < size; i++){
            put(keys.get(i), values.get(i));
        }
        return this;
    }
    
    public MapBuilder<K,V,M> putAll(K[] keys, V[] values){
        int size = keys.length;
        if(size != values.length){
            throw new IllegalArgumentException("Keys size does not correspond to value size:"+size+" to "+values.length);
        }
        for(int i = 0; i < size; i++){
            put(keys[i], values[i]);
        }
        return this;
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
