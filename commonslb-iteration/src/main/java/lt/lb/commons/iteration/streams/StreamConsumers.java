package lt.lb.commons.iteration.streams;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;

/**
 *
 * @author laim0nas100
 * @param <X> element type
 * @param <M> implementation type
 */
public interface StreamConsumers<X, M extends DecoratableStream<X, M>> extends StreamExtension<X, M> {

    /**
     * Iterate through all pairs using provided {@link BiConsumer}. Same as
     * using
     * {@link java.util.stream.Stream#reduce(java.util.function.BinaryOperator)};
     *
     * @param action
     */
    public default void forPairs(BiConsumer<? super X, ? super X> action) {
        Objects.requireNonNull(action, "Action must not be null");
        me().reduce((left, right) -> {
            action.accept(left, right);
            return right;
        });
    }

    /**
     * Iterate through all items with alongside an index
     *
     * @param action
     */
    public default void forIndexed(BiConsumer<Integer, ? super X> action) {
        Objects.requireNonNull(action, "Action must not be null");
        AtomicInteger index = new AtomicInteger(0);
        me().forEachOrdered(item -> {
            action.accept(index.getAndIncrement(), item);
        });
    }
}
