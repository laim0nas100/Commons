package lt.lb.commons.iteration.streams.extendable;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import lt.lb.uncheckedutils.SafeOpt;

/**
 *
 * @author laim0nas100
 */
public interface StreamOptFilters<X, M extends DecoratableStream<X, M>> extends StreamExtension<X, M> {

    public default <R> DecoratableStream<R, ?> mapOptional(Function<? super X, Optional<? extends R>> mapper) {
        Objects.requireNonNull(mapper, "Mapper must not be null");
        return me().map(mapper).filter(item -> item.isPresent()).map(m -> m.get());
    }

    public default <R> DecoratableStream<R, ?> mapSafeOpt(Function<? super X, SafeOpt<? extends R>> mapper) {
        Objects.requireNonNull(mapper, "Mapper must not be null");
        return me().map(mapper).filter(item -> item.isPresent()).map(m -> m.get());
    }

}
