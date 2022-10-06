package experimental.jpa.lazy;

/**
 *
 * @author laim0nas100
 */
public interface LazyLoaderIds<ID, T> extends LazyLoader<ID, T> {

    LazyLoadResult<ID> loadIds(LazyLoadContext ctx);
}
