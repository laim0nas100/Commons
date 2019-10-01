package lt.lb.commons.caller;

import java.util.function.BiFunction;
import java.util.function.Function;

/**
 *
 * @author laim0nas100
 */
public interface CallerForBuilder<R, T> {

    /**
     * Create recursive calls for each (index,item) pair in iterator.
     *
     * @param contFunction
     * @return
     */
    public CallerForBuilderEach<R, T> forEachCall(BiFunction<Integer, R, Caller<T>> contFunction);

    /**
     * Create recursive calls for each item in iterator.
     *
     * @param contFunction
     * @return
     */
    public default CallerForBuilderEach<R, T> forEachCall(Function<R, Caller<T>> contFunction) {
        return this.forEachCall((i, item) -> contFunction.apply(item));
    }
}
