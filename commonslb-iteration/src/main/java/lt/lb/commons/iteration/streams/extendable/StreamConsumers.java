package lt.lb.commons.iteration.streams.extendable;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;

/**
 *
 * @author laim0nas100
 * @param <X> element type
 * @param <M> implementation type
 */
public interface StreamConsumers<X, M extends DelegatingStream<X, M>> extends StreamExtension<X, M> {
    
    public default void forPairs(BiConsumer<? super X, ? super X> action) {
        Objects.requireNonNull(action, "Action must not be null");
        me().reduce((left, right) -> {
            action.accept(left, right);
            return right;
        });
    }
    
    public default void forIndexed(BiConsumer<Integer, ? super X> action) {
        Objects.requireNonNull(action, "Action must not be null");
        AtomicInteger index = new AtomicInteger(0);
        me().forEachOrdered(item -> {
            action.accept(index.getAndIncrement(), item);
        });
    }
}
