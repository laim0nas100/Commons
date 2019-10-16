package lt.lb.commons.caller;

import java.util.Collection;
import java.util.Iterator;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Stream;
import lt.lb.commons.iteration.ReadOnlyIterator;

/**
 * @param <T> the main type of Caller product
 * @param <R> type that iteration happens
 * @author laim0nas100
 */
public class CallerForBuilder<R, T> {

    protected boolean threaded = false;
    protected ReadOnlyIterator<R> iter;
    protected BiFunction<Integer, R, Caller<T>> contFunction;
    protected BiFunction<Integer, T, CallerFlowControl<T>> thenFunction;
    protected Caller<T> afterwards;

    public CallerForBuilder() {
    }

    /**
     *
     * @param stream items to iterate
     * @return builder
     */
    public CallerForBuilder<R, T> with(Stream<R> stream) {
        return with(ReadOnlyIterator.of(stream));
    }

    /**
     *
     * @param iterator items to iterate
     * @return builder
     */
    public CallerForBuilder<R, T> with(Iterator<R> iterator) {
        return with(ReadOnlyIterator.of(iterator));
    }

    /**
     *
     * @param collection items to iterate
     * @return builder
     */
    public CallerForBuilder<R, T> with(Collection<R> collection) {
        return with(ReadOnlyIterator.of(collection));
    }

    /**
     *
     * @param array items to iterate
     * @return builder
     */
    public CallerForBuilder<R, T> with(R... array) {
        return with(ReadOnlyIterator.of(array));
    }

    /**
     *
     * @param iterator items to iterate
     * @return builder
     */
    public CallerForBuilder<R, T> with(ReadOnlyIterator<R> iterator) {
        this.iter = iterator;
        return this;
    }

    /**
     * @param afterwards Caller when iterator runs out of items (or never had
     * them to begin with) so {@code for} loop never exited inside or exited
     * with break condition.
     * @return builder
     */
    public CallerForBuilder<R, T> afterwards(Caller<T> afterwards) {
        this.afterwards = afterwards;
        return this;

    }

    /**
     *
     * @return created Caller that models such loop
     */
    public Caller<T> build() {
        Objects.requireNonNull(afterwards);
        Objects.requireNonNull(iter);
        Objects.requireNonNull(contFunction);
        Objects.requireNonNull(thenFunction);
        if(threaded){
            return CallerImpl.ofIteratorLazyThreaded(afterwards, iter, contFunction, thenFunction);
        }else{
            return CallerImpl.ofIteratorLazy(afterwards, iter, contFunction, thenFunction);
        }
        
    }

    /**
     * How to evaluate each item ignoring indices
     *
     * @param thenFunction evaluation function that gets how to proceed in the
     * middle of a {@code for} loop
     * @return builder
     */
    public CallerForBuilder<R, T> evaluate(Function<T, CallerFlowControl<T>> thenFunction) {
        Objects.requireNonNull(thenFunction);
        return evaluate((i, item) -> thenFunction.apply(item));
    }

    /**
     * How to evaluate each item
     *
     * @param thenFunction evaluation function that gets how to proceed in the
     * middle of a {@code for} loop
     * @return builder
     */
    public CallerForBuilder<R, T> evaluate(BiFunction<Integer, T, CallerFlowControl<T>> thenFunction) {
        this.thenFunction = thenFunction;
        return this;
    }

    /**
     * Create recursive calls for each item in iterator.
     *
     * @param contFunction
     * @return
     */
    public CallerForBuilder<R, T> forEachCall(Function<R, Caller<T>> contFunction) {
        Objects.requireNonNull(contFunction);
        return this.forEachCall((i, item) -> contFunction.apply(item));
    }

    /**
     * Create recursive calls for each (index,item) pair in iterator.
     *
     * @param contFunction
     * @return
     */
    public CallerForBuilder<R, T> forEachCall(BiFunction<Integer, R, Caller<T>> contFunction) {
        this.contFunction = contFunction;
        return this;
    }

}
