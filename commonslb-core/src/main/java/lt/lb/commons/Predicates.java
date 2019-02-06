package lt.lb.commons;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import lt.lb.commons.containers.tuples.Tuple;
import lt.lb.commons.interfaces.Equator;

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
     *
     * @param <T>
     * @param equator on how compare elements
     * @return Predicate to use in a stream
     */
    public static <T> Predicate<T> filterDistinct(Equator.HashEquator<T> equator) {

        return new Predicate<T>() {
            LinkedHashMap<Object, T> kept = new LinkedHashMap<>();

            @Override
            public boolean test(T t) {
                Object hash = equator.getHashable(t);
                if (kept.containsKey(hash)) {
                    return false;
                } else {
                    kept.put(hash, t);
                    return true;
                }
            }
        };
    }

    /**
     *
     * @param <T> type
     * @param equator equality condition
     * @return Predicate to use in a stream
     */
    public static <T> Predicate<T> filterDistinct(Equator<T> equator) {

        return new Predicate<T>() {
            LinkedList<T> kept = new LinkedList<>();

            @Override
            public boolean test(T t) {
                Optional<Tuple<Integer, T>> find = F.find(kept, (i, item) -> equator.equals(t, item));
                boolean toKeep = !find.isPresent();
                if (toKeep) {
                    kept.add(t);
                }
                return toKeep;
            }
        };
    }
}
