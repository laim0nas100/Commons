package lt.lb.commons.iteration.streams.extendable;

import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 *
 * @author laim0nas100
 * @param <X> element type
 * @param <M> implementation type
 */
public interface StreamFilters<X, M extends DelegatingStream<X, M>> extends StreamExtension<X, M> {

    public default <R> DelegatingStream<R, ?> select(Class<R> cls) {
        Objects.requireNonNull(cls, "Class must not be null");
        return (DelegatingStream<R, ?>) me().filter(item -> cls.isInstance(item));
    }
    
    public default M nonNull() {
        return me().filter(Objects::nonNull);
    }

    public default M remove(Predicate<? super X> predicate) {
        Objects.requireNonNull(predicate, "Predicate must not be null");
        return me().filter(predicate.negate());
    }

    public default M without(X value) {
        return value == null ? nonNull() : remove(value::equals);
    }

    public default M without(X... values) {
        if (values.length == 0) {
            return me();
        }
        if (values.length == 1) {
            return without(values[0]);
        }
        return remove(Arrays.asList(values)::contains);
    }

    public default M in(Collection<? extends X> col) {
        Objects.requireNonNull(col, "Supplied collection is null");
        if (col.isEmpty()) {
            return me().intermediate(s -> Stream.empty());
        }
        return me().filter(col::contains);
    }

    public default M notIn(Collection<? extends X> col) {
        Objects.requireNonNull(col, "Supplied collection is null");
        return remove(col::contains);
    }

}
