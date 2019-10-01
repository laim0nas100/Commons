package lt.lb.commons.caller;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 *
 * @author laim0nas100
 */
public interface CallerForBuilderEach<R, T> {

    /**
     * Lazy evaluation. How to evaluate each item ignoring indices
     *
     * @param thenFunction evaluation function that gets how to proceed in the
     * middle of a {@code for} loop
     * @return final builder stage
     */
    public default CallerForBuilderEnd<R, T> evaluateLazy(Function<T, CallerForContinue<T>> thenFunction) {
        Objects.requireNonNull(thenFunction);
        return evaluate(true, thenFunction);
    }

    /**
     * Lazy evaluation. How to evaluate each item
     *
     * @param thenFunction evaluation function that gets how to proceed in the
     * middle of a {@code for} loop
     * @return final builder stage
     */
    public default CallerForBuilderEnd<R, T> evaluateLazy(BiFunction<Integer, T, CallerForContinue<T>> thenFunction) {
        Objects.requireNonNull(thenFunction);
        return evaluate(true, thenFunction);
    }

    /**
     * Eager evaluation. How to evaluate each item ignoring indices
     *
     * @param thenFunction evaluation function that gets how to proceed in the
     * middle of a {@code for} loop
     * @return final builder stage
     */
    public default CallerForBuilderEnd<R, T> evaluateEager(Function<T, CallerForContinue<T>> thenFunction) {
        Objects.requireNonNull(thenFunction);
        return evaluate(false, thenFunction);
    }

    /**
     * Eager evaluation. How to evaluate each item
     *
     * @param thenFunction evaluation function that gets how to proceed in the
     * middle of a {@code for} loop
     * @return final builder stage
     */
    public default CallerForBuilderEnd<R, T> evaluateEager(BiFunction<Integer, T, CallerForContinue<T>> thenFunction) {
        Objects.requireNonNull(thenFunction);
        return evaluate(false, thenFunction);
    }

    /**
     * How to evaluate each item ignoring indices
     *
     * @param lazy evaluation policy
     * @param thenFunction evaluation function that gets how to proceed in the
     * middle of a {@code for} loop
     * @return final builder stage
     */
    public default CallerForBuilderEnd<R, T> evaluate(boolean lazy, Function<T, CallerForContinue<T>> thenFunction) {
        return evaluate(lazy, (i, item) -> thenFunction.apply(item));
    }

    /**
     * How to evaluate each item
     *
     * @param lazy evaluation policy
     * @param thenFunction evaluation function that gets how to proceed in the
     * middle of a {@code for} loop
     * @return final builder stage
     */
    public CallerForBuilderEnd<R, T> evaluate(boolean lazy, BiFunction<Integer, T, CallerForContinue<T>> thenFunction);
}
