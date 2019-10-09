package lt.lb.commons.caller;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * @param <T> the main type of Caller product
 * @author laim0nas100
 */
public class CallerWhileBuilder<T> {

    protected Supplier<Boolean> condition;
    protected Supplier<Caller<T>> contFunction;
    protected Function<T, CallerFlowControl<T>> thenFunction;
    protected Caller<T> afterwards;
    protected boolean dowhile = false;

    public CallerWhileBuilder() {
    }

    /**
     * @param afterwards Caller when {@code while} loop exited inside.
     * @return builder
     */
    public CallerWhileBuilder<T> afterwards(Caller<T> afterwards) {
        this.afterwards = afterwards;
        return this;
    }
    /**
     * 
     * @param condition {@code while} loop condition
     * @return builder
     */
    public CallerWhileBuilder<T> whilst(Supplier<Boolean> condition){
        this.condition = condition;
        return this;
    }

    /**
     *
     * @return created Caller that models such loop
     */
    public Caller<T> build() {
        Objects.requireNonNull(afterwards);
        Objects.requireNonNull(condition);
        Objects.requireNonNull(contFunction);
        Objects.requireNonNull(thenFunction);
        if (dowhile) {
            return CallerImpl.ofDoWhileLoop(afterwards, condition, contFunction, thenFunction);
        } else {
            return CallerImpl.ofWhileLoop(afterwards, condition, contFunction, thenFunction);
        }
    }

    /**
     * How to evaluate each item ignoring indices
     *
     * @param thenFunction evaluation function that gets how to proceed in the
     * middle of a {@code while} loop
     * @return builder
     */
    public CallerWhileBuilder<T> evaluate(Function<T, CallerFlowControl<T>> thenFunction) {
        this.thenFunction = thenFunction;
        return this;
    }

    /**
     * Create recursive calls
     *
     * @param contFunction
     * @return
     */
    public CallerWhileBuilder<T> forEachCall(Supplier<Caller<T>> contFunction) {
        this.contFunction = contFunction;
        return this;
    }
}
