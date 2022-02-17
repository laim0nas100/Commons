package lt.lb.commons.iteration.streams;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;
import lt.lb.commons.Equator;
import lt.lb.commons.Ins;
import lt.lb.commons.Lazy;

/**
 *
 * @author laim0nas100
 * @param <X> element type
 * @param <M> implementation type
 */
public interface StreamFilters<X, M extends DecoratableStream<X, M>> extends StreamExtension<X, M> {

    /**
     * Filter items to be instance of given type and cast the stream to the
     * given type
     *
     * @param <R>
     * @param cls
     * @return
     */
    public default <R> DecoratableStream<R, ?> select(Class<R> cls) {
        Objects.requireNonNull(cls, "Class must not be null");
        return (DecoratableStream<R, ?>) me().filter(item -> cls.isInstance(item));
    }

    /**
     * Filter stream to be of any (disjunction) of the given types. Null class
     * means accept null items.
     *
     * @param cls
     * @return
     */
    public default M withAnyTypes(Class... cls) {
        Objects.requireNonNull(cls, "Select any types array must not be null");

        if (cls.length == 0) {
            throw new IllegalArgumentException("Select any types array is empty");
        }
        return me().filter(item -> {
            for (Class type : cls) {
                if (Ins.instanceOf(item, type)) {
                    return true;
                }
            }
            return false;
        });
    }

    /**
     * Filter stream to not be of any of the given types. Null class means
     * remove null items.
     *
     * @param cls
     * @return
     */
    public default M removeTypes(Class... cls) {
        Objects.requireNonNull(cls, "Remove types array must not be null");

        if (cls.length == 0) {
            throw new IllegalArgumentException("Remove types array is empty");
        }
        return me().filter(item -> {
            for (Class type : cls) {
                if (Ins.instanceOf(item, type)) {
                    return false;
                }
            }
            return true;
        });
    }

    /**
     * Filter stream to be of all (conjunction) of the given types
     *
     * @param cls
     * @return
     */
    public default M withAllTypes(Class... cls) {
        Objects.requireNonNull(cls, "Select all types array must not be null");

        if (cls.length == 0) {
            throw new IllegalArgumentException("Select all types array is empty");
        }
        return me().filter(item -> {
            for (Class type : cls) {
                if (!Ins.instanceOf(item, type)) {
                    return false;
                }
            }
            return true;
        });
    }

    /**
     * Filter out null items.
     *
     * @return
     */
    public default M nonNull() {
        return me().filter(Objects::nonNull);
    }

    /**
     * Filter out items that satisfies given predicate.
     *
     * @param predicate
     * @return
     */
    public default M remove(Predicate<? super X> predicate) {
        Objects.requireNonNull(predicate, "Predicate must not be null");
        return me().filter(predicate.negate());
    }

    /**
     * If value is null, filter out null values, otherwise filter out items that
     * satisfies given value equality
     *
     * @param value
     * @return
     */
    public default M without(X value) {
        return value == null ? nonNull() : remove(value::equals);
    }

    /**
     * Filter out all items that satisfied equality of given array of values.
     *
     * @param values
     * @return
     */
    public default M without(X... values) {
        if (values.length == 0) {
            return me();
        }
        if (values.length == 1) {
            return without(values[0]);
        }
        return remove(item -> {
            for (X val : values) {
                if (Objects.equals(val, item)) {
                    return false;
                }
            }
            return true;
        });
    }

    /**
     * The initial collection should not be changed.
     *
     * Leave only items that are present in given collection. If collection is
     * empty, return empty stream
     *
     * @param col
     * @return
     */
    public default M in(Collection<? extends X> col) {
        Objects.requireNonNull(col, "Supplied collection is null");
        if (col.isEmpty()) {
            return me().intermediate(s -> Stream.empty());
        }
        return me().filter(col::contains);
    }

    /**
     * The initial collection should not be changed.
     *
     * Leave only items that are not present in given collection.
     *
     * If collection is empty, return current stream
     *
     * @param col
     * @return
     */
    public default M notIn(Collection<? extends X> col) {
        Objects.requireNonNull(col, "Supplied collection is null");
        if (col.isEmpty()) {
            return me();
        }
        return remove(col::contains);
    }

    /**
     * Create a {@link HashSet} with equality proxy of given collection and
     * {@link Equator}.
     *
     * @param <T>
     * @param col
     * @param equator
     * @return
     */
    public static <T> Set<Equator.EqualityProxy<T>> createEqualityProxySet(Collection<? extends T> col, Equator<? super T> equator) {
        Set<Equator.EqualityProxy<T>> set = new HashSet<>(col.size());
        for (T item : col) {
            set.add(new Equator.EqualityProxy<>(item, equator));
        }
        return set;

    }

    /**
     * The initial collection should not be changed.
     *
     * Leave only items that are present in given collection given equator.
     *
     * If collection is empty, return empty stream.
     *
     * New set created with given equator is frozen at the time the first item
     * is being filtered.
     *
     * @param col
     * @param equator
     * @return
     */
    public default M in(Collection<? extends X> col, Equator<? super X> equator) {
        Objects.requireNonNull(col, "Supplied collection is null");
        Objects.requireNonNull(equator, "Supplied equator is null");
        if (col.isEmpty()) {
            return me().intermediate(s -> Stream.empty());
        }
        Lazy<Set<Equator.EqualityProxy<X>>> lazy = new Lazy<>(() -> createEqualityProxySet(col, equator));
        return me().filter(item -> {
            return lazy.get().contains(new Equator.EqualityProxy<>(item, equator));
        });
    }

    /**
     * The initial collection should not be changed.
     *
     * Leave only items that are present in given collection given equator.
     *
     * If collection is empty, return current stream.
     *
     * New set created with given equator is frozen at the time the first item
     * is being filtered.
     *
     * @param col
     * @param equator
     * @return
     */
    public default M notIn(Collection<? extends X> col, Equator<? super X> equator) {
        Objects.requireNonNull(col, "Supplied collection is null");
        Objects.requireNonNull(equator, "Supplied equator is null");
        if (col.isEmpty()) {
            return me();
        }
        Lazy<Set<Equator.EqualityProxy<X>>> lazy = new Lazy<>(() -> createEqualityProxySet(col, equator));
        return me().filter(item -> {
            return !lazy.get().contains(new Equator.EqualityProxy<>(item, equator));
        });
    }

    /**
     * Use distinct operation but with different equality meaning using the
     * provided equator.
     *
     * @param equator
     * @return
     */
    public default M distinct(Equator<? super X> equator) {
        Objects.requireNonNull(equator, "Supplied equator is null");
        return (M) me().map(s -> new Equator.EqualityProxy<>(s, equator)).distinct().map(m -> m.getValue());
    }

}
