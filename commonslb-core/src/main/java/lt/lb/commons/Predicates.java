package lt.lb.commons;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.function.Predicate;
import lt.lb.commons.containers.values.BooleanValue;
import lt.lb.commons.interfaces.Equator;
import lt.lb.commons.interfaces.Equator.EqualityProxy;

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
    public static <T> Predicate<T> filterDistinct(Equator<T> equator) {

        return new Predicate<T>() {
            ConcurrentHashMap<EqualityProxy<T>, EqualityProxy<T>> kept = new ConcurrentHashMap<>();
            AtomicBoolean foundNull = new AtomicBoolean(false);

            @Override
            public boolean test(T t) {
                if (t == null) {
                    return foundNull.compareAndSet(false, true);
                }

                BooleanValue isNew = BooleanValue.FALSE();
                EqualityProxy<T> equalityHashProxy = new EqualityProxy<>(t, equator);
                kept.computeIfAbsent(equalityHashProxy, k -> {
                    isNew.set(true);
                    return k;
                });

                return isNew.get();
            }
        };
    }

    /**
     * Predicate to filter your streams. Supports multiple threads.
     * Hashing and value property are the same.
     * @param <T>
     * @return 
     */
    public static <T> Predicate<T> filterDistinct(){
        return filterDistinct(Equator.primitiveHashEquator());
    }
}
