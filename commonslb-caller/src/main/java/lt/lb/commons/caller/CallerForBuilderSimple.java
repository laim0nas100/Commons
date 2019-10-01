package lt.lb.commons.caller;

import java.util.Collection;
import java.util.Iterator;
import java.util.function.BiFunction;
import java.util.stream.Stream;
import lt.lb.commons.iteration.ReadOnlyIterator;

/**
 * @param <T> the main type of Caller product
 * @param <R> type that iteration happens
 * @author laim0nas100
 */
public class CallerForBuilderSimple<R, T> implements CallerForBuilder<R, T> {

    private ReadOnlyIterator<R> iter;

    /**
     * @param iterator ReadOnlyIterator that has items
     *
     */
    public CallerForBuilderSimple(ReadOnlyIterator<R> iterator) {
        this.iter = iterator;
    }

    public CallerForBuilderSimple(Stream<R> stream) {
        this(ReadOnlyIterator.of(stream));
    }

    public CallerForBuilderSimple(Iterator<R> iterator) {
        this(ReadOnlyIterator.of(iterator));
    }

    public CallerForBuilderSimple(Collection<R> collection) {
        this(ReadOnlyIterator.of(collection));
    }

    public CallerForBuilderSimple(R... array) {
        this(ReadOnlyIterator.of(array));
    }

    @Override
    public CallerForBuilderEach<R, T> forEachCall(BiFunction<Integer, R, Caller<T>> contFunction) {
        CallerForBuilderMain<R, T> main = new CallerForBuilderMain<>(iter);
        return main.forEachCall(contFunction);
    }
}
