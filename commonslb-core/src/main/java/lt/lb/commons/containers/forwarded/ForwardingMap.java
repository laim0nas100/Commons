package lt.lb.commons.containers.forwarded;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author laim0nas100
 */
public interface ForwardingMap<K, V> extends Map<K, V> {

    Map<K, V> delegate();

    @Override
    public default int size() {
        return delegate().size();
    }

    @Override
    public default boolean isEmpty() {
        return delegate().isEmpty();
    }

    @Override
    public default boolean containsKey(Object key) {
        return delegate().containsKey(key);
    }

    @Override
    public default boolean containsValue(Object value) {
        return delegate().containsValue(value);
    }

    @Override
    public default V get(Object key) {
        return delegate().get(key);
    }

    @Override
    public default V put(K key, V value) {
        return delegate().put(key, value);
    }

    @Override
    public default V remove(Object key) {
        return delegate().remove(key);
    }

    @Override
    public default void putAll(Map<? extends K, ? extends V> m) {
        delegate().putAll(m);
    }

    @Override
    public default void clear() {
        delegate().clear();
    }

    @Override
    public default Set<K> keySet() {
        return delegate().keySet();
    }

    @Override
    public default Collection<V> values() {
        return delegate().values();
    }

    @Override
    public default Set<Entry<K, V>> entrySet() {
        return delegate().entrySet();
    }

}
