package lt.lb.commons.iteration.streams.extendable;

import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Stream;
import lt.lb.commons.Equator;
import lt.lb.commons.Ins;

/**
 *
 * @author laim0nas100
 * @param <X> element type
 * @param <M> implementation type
 */
public interface StreamFilters<X, M extends DecoratableStream<X, M>> extends StreamExtension<X, M> {

    public default <R> DecoratableStream<R, ?> select(Class<R> cls) {
        Objects.requireNonNull(cls, "Class must not be null");
        return (DecoratableStream<R, ?>) me().filter(item -> cls.isInstance(item));
    }

    public default M selectAnyTypes(Class... cls) {
        Objects.requireNonNull(cls, "Select any types array must not be null");

        if (cls.length == 0) {
            throw new IllegalArgumentException("Select any types array is empty");
        }
        return me().filter(item -> Ins.ofNullable(item).instanceOfAny(cls));
    }

    public default M removeTypes(Class... cls) {
        Objects.requireNonNull(cls, "Remove types array must not be null");

        if (cls.length == 0) {
            throw new IllegalArgumentException("Remove types array is empty");
        }
        return me().filter(item -> !Ins.ofNullable(item).instanceOfAny(cls));
    }

    public default M selectAllTypes(Class... cls) {
        Objects.requireNonNull(cls, "Select all types array must not be null");

        if (cls.length == 0) {
            throw new IllegalArgumentException("Select all types array is empty");
        }
        return me().filter(item -> Ins.ofNullable(item).instanceOfAll(cls));
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
        if (col.isEmpty()) {
            return me();
        }
        return remove(col::contains);
    }

    public default M in(Collection<? extends X> col, Equator<? super X> equator) {
        Objects.requireNonNull(col, "Supplied collection is null");
        Objects.requireNonNull(equator, "Supplied equator is null");
        if (col.isEmpty()) {
            return me().intermediate(s -> Stream.empty());
        }
        return me().filter(item -> {
            for (X obj : col) {
                if (Equator.equals(equator, obj, item)) {
                    return true;
                }
            }

            return false;
        });
    }

    public default M notIn(Collection<? extends X> col, Equator<? super X> equator) {
        Objects.requireNonNull(col, "Supplied collection is null");
        Objects.requireNonNull(equator, "Supplied equator is null");
        if (col.isEmpty()) {
            return me();
        }
        return me().filter(item -> {
            for (X obj : col) {
                if (Equator.equals(equator, obj, item)) {
                    return false;
                }
            }
            return true;
        });
    }

    public default M distinct(Equator<? super X> equator) {
        Objects.requireNonNull(equator, "Supplied equator is null");
        return (M) me().map(s -> new Equator.EqualityProxy<>(s, equator)).distinct().map(m -> m.getValue());
    }

}
