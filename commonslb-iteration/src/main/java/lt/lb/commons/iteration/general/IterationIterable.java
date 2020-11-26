package lt.lb.commons.iteration.general;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import lt.lb.commons.iteration.Iter;
import lt.lb.commons.iteration.ReadOnlyIterator;
import lt.lb.commons.iteration.general.result.IterIterableResult;

/**
 *
 * @author laim0nas100
 */
public interface IterationIterable<E extends IterationIterable<E>> extends IterationAbstract<E> {

    public E startingFrom(int from);

    public E endingBefore(int to);

    public default E withInterval(int from, int to) {
        return startingFrom(from).endingBefore(to);
    }

    public <T> Optional<IterIterableResult<T>> find(ReadOnlyIterator<T> iterator, Iter<T> iter);

    public <T> Optional<IterIterableResult<T>> find(List<T> list, Iter<T> iter);

    public <T> Optional<IterIterableResult<T>> find(T[] array, Iter<T> iter);

    public <T> Optional<IterIterableResult<T>> findBackwards(List<T> list, Iter<T> iter);

    public <T> Optional<IterIterableResult<T>> findBackwards(T[] array, Iter<T> iter);

    public default <T> void iterateBackwards(List<T> list, Iter.IterNoStop<T> iter) {
        findBackwards(list, iter);
    }

    public default <T> void iterateBackwards(T[] array, Iter.IterNoStop<T> iter) {
        findBackwards(array, iter);
    }

    public default <T> Optional<IterIterableResult<T>> find(Iterable<T> iterable, Iter<T> iter) {
        return find(ReadOnlyIterator.of(iterable), iter);
    }

    public default <T> Optional<IterIterableResult<T>> find(Iterator<T> iterator, Iter<T> iter) {
        return find(ReadOnlyIterator.of(iterator), iter);
    }

    public default <T> Optional<IterIterableResult<T>> find(Stream<T> stream, Iter<T> iter) {
        return find(ReadOnlyIterator.of(stream), iter);
    }

    public default <T> void iterate(List<T> list, Iter.IterNoStop<T> iter) {
        find(list, iter);
    }

    public default <T> void iterate(T[] array, Iter.IterNoStop<T> iter) {
        find(array, iter);
    }

    public default <T> void iterate(Iterator<T> iterator, Iter.IterNoStop<T> iter) {
        find(ReadOnlyIterator.of(iterator), iter);
    }

    public default <T> void iterate(Iterable<T> list, Iter.IterNoStop<T> iter) {
        find(ReadOnlyIterator.of(list), iter);
    }

    public default <T> void iterate(ReadOnlyIterator<T> iterator, Iter.IterNoStop<T> iter) {
        find(iterator, iter);
    }

    public default <T> void iterate(Stream<T> stream, Iter.IterNoStop<T> iter) {
        find(ReadOnlyIterator.of(stream), iter);
    }

}
