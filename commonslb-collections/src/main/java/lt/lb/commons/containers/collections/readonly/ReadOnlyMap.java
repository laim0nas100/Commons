package lt.lb.commons.containers.collections.readonly;

import java.util.Map;
import java.util.function.BiFunction;

/**
 *
 * @author laim0nas100
 */
public interface ReadOnlyMap<K, V> extends Map<K, V> {

    @Override
    public default boolean isEmpty() {
        return size() == 0;
    }

    @Override
    public default V put(K key, V value) {
        throw new UnsupportedOperationException("Read only instance of " + getClass());
    }

    @Override
    public default V remove(Object key) {
        throw new UnsupportedOperationException("Read only instance of " + getClass());
    }

    @Override
    public default void putAll(Map<? extends K, ? extends V> m) {
        throw new UnsupportedOperationException("Read only instance of " + getClass());
    }

    @Override
    public default void clear() {
        throw new UnsupportedOperationException("Read only instance of " + getClass());
    }

    @Override
    public default V putIfAbsent(K key, V value) {
        throw new UnsupportedOperationException("Read only instance of " + getClass());
    }

    @Override
    public default boolean remove(Object key, Object value) {
        throw new UnsupportedOperationException("Read only instance of " + getClass());
    }

    @Override
    public default boolean replace(K key, V oldValue, V newValue) {
        throw new UnsupportedOperationException("Read only instance of " + getClass());
    }

    @Override
    public default V replace(K key, V value) {
        throw new UnsupportedOperationException("Read only instance of " + getClass());
    }

    @Override
    public default V merge(K key, V value, BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
        throw new UnsupportedOperationException("Read only instance of " + getClass());
    }

}
