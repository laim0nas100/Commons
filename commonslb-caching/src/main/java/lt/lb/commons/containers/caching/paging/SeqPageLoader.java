package lt.lb.commons.containers.caching.paging;

import java.util.Objects;
import java.util.function.Function;
import lt.lb.commons.containers.caching.paging.impl.PageMappings;

/**
 *
 * @author laim0nas100
 * @param <T>
 */
public interface SeqPageLoader<T> extends PageLoader<Long, T> {

    public default SeqLoadedPage<T> loadPage(int index, int size) {
        return loadPage((long) index, size);
    }

    @Override
    public default SeqLoadedPage<T> loadPage(Long index, int size) {
        return loadPage(index, (long) size);
    }

    @Override
    public SeqLoadedPage<T> loadPage(Long index, long size);

    @Override
    public default <V> SeqPageLoader<V> mapped(Function<T, V> func) {
        Objects.requireNonNull(func);
        return (Long key, long size) -> new PageMappings.MappedSeqPage<>(loadPage(key, size), func);
    }

}
