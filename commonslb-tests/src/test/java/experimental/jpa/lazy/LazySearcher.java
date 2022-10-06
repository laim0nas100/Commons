package experimental.jpa.lazy;

import java.util.function.Function;

/**
 *
 * @author laim0nas100
 */
public interface LazySearcher<ID, Search> {
    
    public LazyLoadResult<ID> search(LazyLoadContext ctx, Search search);

    public default Function<LazyLoadContext, LazyLoadResult<ID>> usingSearch(Search search) {
        return ctx -> search(ctx, search);
    }

}
