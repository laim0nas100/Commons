package lt.lb.commons.containers.caching.paging;

import java.util.Map;
import java.util.function.Function;
import lt.lb.commons.containers.caching.paging.impl.PageMappings;

/**
 *
 *
 * @param <K>
 * @param <T>
 * @author laim0nas100
 */
public interface LoadedPage<K, T> extends Function<K, T> {

    public T get(K id);

    public long size();

    public boolean contains(K id);

    @Override
    public default T apply(K t) {
        return get(t);
    }

    public default T returnEmpty() {
        return null;
    }

}
