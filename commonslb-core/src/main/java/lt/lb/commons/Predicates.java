package lt.lb.commons;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.function.Predicate;
import lt.lb.commons.containers.BooleanValue;
import lt.lb.commons.interfaces.Equator;
import lt.lb.commons.interfaces.Equator.EqualityHashProxy;

/**
 *
 * @author laim0nas100
 */
public class Predicates {

    public static <T> Predicate<T> isEqual(T target) {
        return Predicate.isEqual(target);
    }

    public static <T> Predicate<T> isNotEqual(T target) {
        return isEqual(target).negate();
    }

    public static <T> Predicate<T> allEqual(T... objects) {
        return t -> ArrayOp.all(a -> Objects.equals(t, a), objects);
    }

    public static <T> Predicate<T> anyEqual(T... objects) {
        return t -> ArrayOp.any(a -> Objects.equals(t, a), objects);
    }

    /**
     * Check if argument is null
     *
     * @return
     */
    public static <T> Predicate<T> isNull() {
        return (t) -> t == null;
    }

    /**
     * Check if argument is not null
     *
     * @return
     */
    public static <T> Predicate<T> isNotNull() {
        return (t) -> t != null;
    }

    public static <T, E> Predicate<T> ofMapping(Predicate<E> pred, Function<T, E> mapping) {
        return t -> pred.test(mapping.apply(t));
    }

    /**
     * Predicate to filter your streams. Supports multiple threads.
     *
     * @param <T>
     * @param equator on how compare elements
     * @return Predicate to use in a stream
     */
    public static <T> Predicate<T> filterDistinct(Equator.HashEquator<T> equator) {

        return new Predicate<T>() {
            ConcurrentHashMap<Object, EqualityHashProxy<T>> kept = new ConcurrentHashMap<>();
            AtomicBoolean foundNull = new AtomicBoolean(false);

            @Override
            public boolean test(T t) {
                if (t == null) {
                    return foundNull.compareAndSet(false, true);
                }

                BooleanValue isNew = BooleanValue.FALSE();
                EqualityHashProxy<T> equalityHashProxy = new EqualityHashProxy<>(t, equator);
                kept.computeIfAbsent(equalityHashProxy, k -> {
                    isNew.accept(true);
                    return equalityHashProxy;
                });

                return isNew.get();
            }
        };
    }

    /**
     * Predicate to filter your streams. Supports multiple threads.
     *
     * @param <T>
     * @param equator equality condition
     * @return Predicate to use in a stream
     */
    public static <T> Predicate<T> filterDistinct(Equator<T> equator) {

        return new Predicate<T>() {
            ConcurrentLinkedDeque<T> kept = new ConcurrentLinkedDeque<>();
            AtomicBoolean foundNull = new AtomicBoolean(false);

            @Override
            public boolean test(T t) {

                if (t == null) {
                    return foundNull.compareAndSet(false, true);
                }

                boolean toKeep = !F.find(kept, (i, item) -> equator.equals(t, item)).isPresent();

                if (toKeep) {
                    kept.add(t);
                }
                return toKeep;
            }
        };
    }
}
