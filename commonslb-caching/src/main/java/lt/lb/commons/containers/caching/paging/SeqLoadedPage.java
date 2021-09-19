package lt.lb.commons.containers.caching.paging;

import java.util.List;
import lt.lb.commons.containers.caching.paging.impl.PageMappings;

/**
 *
 * @author laim0nas100
 * @param <T>
 */
public interface SeqLoadedPage<T> extends LoadedPage<Long, T> {

    public default boolean contains(int index) {
        return contains((long) index);
    }

    public default T get(int index) {
        return get((long) index);
    }

    @Override
    public T get(Long index);

    @Override
    public default boolean contains(Long index) {
        return index >= 0L && index < size();
    }

    public default List<T> toList() {
        return new PageMappings.ListMapping<>(this);
    }

}
