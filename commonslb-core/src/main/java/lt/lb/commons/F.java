package lt.lb.commons;

import java.util.function.Predicate;

/**
 *
 * @author laim0nas100
 */
public abstract class F {

    /**
     * Convenience wrapped if check instead of ? operator avoid duplicate
     * computation of trueCase when using ? operator.
     *
     * @param <T>
     * @param trueCase
     * @param falseCase
     * @param pred
     * @return
     */
    public static <T> T ifWrap(T trueCase, T falseCase, Predicate<T> pred) {
        return Nulls.requireNonNull(pred, "Predicate is null").test(trueCase) ? trueCase : falseCase;
    }

    /**
     * Static cast function. Cast operation is quite significant, so this makes
     * is searchable.
     *
     * @param <T>
     * @param <E>
     * @param ob
     * @return
     * @throws ClassCastException
     */
    public static <T extends E, E> T cast(E ob) throws ClassCastException {
        return (T) ob;
    }

}
