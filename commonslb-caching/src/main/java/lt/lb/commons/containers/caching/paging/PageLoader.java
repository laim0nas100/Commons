package lt.lb.commons.containers.caching.paging;

import java.util.Objects;
import java.util.function.Function;
import lt.lb.commons.containers.caching.paging.impl.PageMappings;

/**
 *
 * @author laim0nas100
 * @param <K>
 * @param <T>
 */
public interface PageLoader<K, T> {

    public LoadedPage<K, T> loadPage(K key, long size);

    public default LoadedPage<K, T> loadPage(K key, int size) {
        return loadPage(key, (long) size);
    }

    public default <V> PageLoader<K, V> mapped(Function<T, V> func) {
        Objects.requireNonNull(func);
        return (K key, long size) -> new PageMappings.MappedPage<>(loadPage(key, size), func);
    }

}
