package lt.lb.commons;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

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

}
