package lt.lb.commons;

import java.util.*;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 *
 * @author laim0nas100
 */
public class F {

    /**
     * ad hoc empty object to be used instead of null, so that null becomes
     * available as a value.
     */
    public static final Object EMPTY_OBJECT = new Object() {
        @Override
        public String toString() {
            return "Empty object";
        }
    };

    /**
     * Convenience wrapped null check instead of ? operator avoid duplication of
     * object when using ? operator. If java 9 is available, use
     * Object.requireNotNullElse
     *
     * @param <T>
     * @param object
     * @param nullCase
     * @return
     */
    public static <T> T nullWrap(T object, T nullCase) {
        return object == null ? Objects.requireNonNull(nullCase, "nullCase is null") : object;
    }

    /**
     * Convenience wrapped null check instead of ? operator avoid duplication of
     * object when using ? operator. If java 9 is available, use
     * Object.requireNotNullElseGet
     *
     * @param <T>
     * @param object
     * @param nullCaseSup
     * @return
     */
    public static <T> T nullSupp(T object, Supplier<T> nullCaseSup) {
        if (object == null) {
            return Objects.requireNonNull(Objects.requireNonNull(nullCaseSup, "supplier").get(), "supplier.get()");
        }
        return object;
    }

    /**
     * Convenience wrapped if check instead of ? operator avoid duplication of
     * trueCase when using ? operator
     *
     * @param <T>
     * @param trueCase
     * @param falseCase
     * @param pred
     * @return
     */
    public static <T> T ifWrap(T trueCase, T falseCase, Predicate<T> pred) {
        return pred.test(trueCase) ? trueCase : falseCase;
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
