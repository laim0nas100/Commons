package lt.lb.commons.caller;

import java.util.Objects;
import java.util.function.BiFunction;
import lt.lb.commons.iteration.ReadOnlyIterator;

/**
 * @param <T> the main type of Caller product
 * @param <R> type that iteration happens
 * @author laim0nas100
 *
 */
class CallerForBuilderMain<R, T> implements CallerForBuilder<R, T>, CallerForBuilderEach<R, T>, CallerForBuilderEnd<R, T> {

    private ReadOnlyIterator<R> iter;
    private BiFunction<Integer, R, Caller<T>> contFunction;
    private BiFunction<Integer, T, CallerForContinue<T>> thenFunction;
    private boolean lazy;

    CallerForBuilderMain(ReadOnlyIterator<R> iterator) {
        this.iter = iterator;
    }

    @Override
    public Caller<T> afterwards(Caller<T> afterwards) {
        Objects.requireNonNull(afterwards);
        if (lazy) {
            return CallerImpl.ofIteratorLazy(afterwards, iter, contFunction, thenFunction);
        } else {
            return CallerImpl.ofIteratorChain(afterwards, iter, contFunction, thenFunction);
        }
    }

    @Override
    public CallerForBuilderEnd<R, T> evaluate(boolean lazy, BiFunction<Integer, T, CallerForContinue<T>> thenFunction) {
        Objects.requireNonNull(thenFunction);
        this.lazy = lazy;
        this.thenFunction = thenFunction;
        return this;
    }

    @Override
    public CallerForBuilderEach<R, T> forEachCall(BiFunction<Integer, R, Caller<T>> contFunction) {
        Objects.requireNonNull(contFunction);
        this.contFunction = contFunction;
        return this;
    }

}
