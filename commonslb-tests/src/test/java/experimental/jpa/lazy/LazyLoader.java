package experimental.jpa.lazy;

import java.util.List;

/**
 *
 * @author laim0nas100
 * @param <ID>
 * @param <T>
 */
public interface LazyLoader<ID,T> {

    List<T> lazyLoad(LazyLoadResult<ID> paramList);
}
