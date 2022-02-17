package lt.lb.commons.iteration.streams;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import lt.lb.uncheckedutils.SafeOpt;

/**
 *
 * @author laim0nas100
 * @param <X> element type
 * @param <M> implementation type
 */
public interface StreamOptFilters<X, M extends DecoratableStream<X, M>> extends StreamExtension<X, M> {

    /**
     * Produces and unwraps {@link Optional}.
     *
     * If {@link Optional} is null, treats it as non-present
     *
     * @param <R>
     * @param mapper
     * @return
     */
    public default <R> DecoratableStream<R, ?> mapOptional(Function<? super X, Optional<? extends R>> mapper) {
        Objects.requireNonNull(mapper, "Mapper must not be null");
        return me().map(mapper).filter(item -> item == null ? false : item.isPresent()).map(m -> m.get());
    }

    /**
     * Produces and unwraps {@link SafeOpt} ignoring the error.
     *
     * If {@link SafeOpt} is null, treats it as non-present with no error.
     *
     * @param <R>
     * @param mapper
     * @return
     */
    public default <R> DecoratableStream<R, ?> mapSafeOpt(Function<? super X, SafeOpt<? extends R>> mapper) {
        Objects.requireNonNull(mapper, "Mapper must not be null");
        return me().map(mapper).filter(item -> item == null ? false : item.isPresent()).map(m -> m.get());
    }

    /**
     * Produces and unwraps {@link SafeOpt} optionally consuming the error.
     *
     * If {@link SafeOpt} is null, treats it as non-present with no error.
     *
     * @param <R>
     * @param errorConsumer
     * @param mapper
     * @return
     */
    public default <R> DecoratableStream<R, ?> mapSafeOpt(Consumer<Throwable> errorConsumer, Function<? super X, SafeOpt<? extends R>> mapper) {
        Objects.requireNonNull(errorConsumer, "Error consumer must not be null");
        Objects.requireNonNull(mapper, "Mapper must not be null");
        return me().map(mapper)
                .filter(item -> item == null ? false : item.peekError(errorConsumer).isPresent())
                .map(m -> m.get());
    }

}
