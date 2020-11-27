package lt.lb.commons.iteration.general;

import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
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

    /**
     * Set staring index (inclusive)
     *
     * @param from inclusive starting interval index
     * @return
     */
    public E startingFrom(int from);

    /**
     * Set ending index (exclusive)
     *
     * @param to exclusive ending interval index
     * @return
     */
    public E endingBefore(int to);

    /**
     * Set interval. [from , to). Inclusive : Exclusive.
     *
     * @param from
     * @param to
     * @return
     */
    public default E withInterval(int from, int to) {
        return startingFrom(from).endingBefore(to);
    }

    /**
     * Linear iteration of the given interval until Iter logic returns true, or iteration exhausts all
     * elements
     */
    public <T> Optional<IterIterableResult<T>> find(ReadOnlyIterator<T> iterator, Iter<T> iter);

    /**
     * Linear iteration of the given interval until Iter logic returns true, or iteration exhausts all
     * elements
     */
    public <T> Optional<IterIterableResult<T>> find(List<T> list, Iter<T> iter);

    /**
     * Linear iteration of the given interval until Iter logic returns true, or iteration exhausts all
     * elements
     */
    public <T> Optional<IterIterableResult<T>> find(T[] array, Iter<T> iter);

    /**
     * Linear backward iteration of the given interval until Iter logic returns true, or iteration
     * exhausts all elements
     */
    public <T> Optional<IterIterableResult<T>> findBackwards(List<T> list, Iter<T> iter);

    /**
     * Linear backward iteration of the given interval until Iter logic returns true, or iteration
     * exhausts all elements
     */
    public <T> Optional<IterIterableResult<T>> findBackwards(Deque<T> deque, Iter<T> iter);

    /**
     * Linear backward iteration of the given interval until iteration exhausts all elements
     */
    public default <T> void findBackwards(LinkedList<T> list, Iter.IterNoStop<T> iter) {
        findBackwards((List<T>) list, iter);
    }

    /**
     * Linear backward iteration of the given interval until Iter logic returns true, or iteration
     * exhausts all elements
     */
    public <T> Optional<IterIterableResult<T>> findBackwards(T[] array, Iter<T> iter);

    /**
     * Linear backward iteration of the given interval until iteration exhausts all elements
     */
    public default <T> void iterateBackwards(LinkedList<T> list, Iter.IterNoStop<T> iter) {
        findBackwards((List<T>) list, iter);
    }

    /**
     * Linear backward iteration of the given interval until iteration exhausts all elements
     */
    public default <T> void iterateBackwards(List<T> list, Iter.IterNoStop<T> iter) {
        findBackwards(list, iter);
    }

    /**
     * Linear backward iteration of the given interval until iteration exhausts all elements
     */
    public default <T> void iterateBackwards(Deque<T> deque, Iter.IterNoStop<T> iter) {
        findBackwards(deque, iter);
    }

    /**
     * Linear backward iteration of the given interval until iteration exhausts all elements
     */
    public default <T> void iterateBackwards(T[] array, Iter.IterNoStop<T> iter) {
        findBackwards(array, iter);
    }

    /**
     * Linear iteration of the given interval until Iter logic returns true, or iteration exhausts all
     * elements
     */
    public default <T> Optional<IterIterableResult<T>> find(Iterable<T> iterable, Iter<T> iter) {
        return find(ReadOnlyIterator.of(iterable), iter);
    }

    /**
     * Linear iteration of the given interval until Iter logic returns true, or iteration exhausts all
     * elements
     */
    public default <T> Optional<IterIterableResult<T>> find(Iterator<T> iterator, Iter<T> iter) {
        return find(ReadOnlyIterator.of(iterator), iter);
    }

    /**
     * Linear iteration of the given interval until Iter logic returns true, or iteration exhausts all
     * elements
     */
    public default <T> Optional<IterIterableResult<T>> find(Stream<T> stream, Iter<T> iter) {
        return find(ReadOnlyIterator.of(stream), iter);
    }

    /**
     * Linear iteration of the given interval until iteration exhausts all elements
     */
    public default <T> void iterate(List<T> list, Iter.IterNoStop<T> iter) {
        find(list, iter);
    }

    /**
     * Linear iteration of the given interval until iteration exhausts all elements
     */
    public default <T> void iterate(T[] array, Iter.IterNoStop<T> iter) {
        find(array, iter);
    }

    /**
     * Linear iteration of the given interval until iteration exhausts all elements
     */
    public default <T> void iterate(Iterator<T> iterator, Iter.IterNoStop<T> iter) {
        find(ReadOnlyIterator.of(iterator), iter);
    }

    /**
     * Linear iteration of the given interval until iteration exhausts all elements
     */
    public default <T> void iterate(Iterable<T> list, Iter.IterNoStop<T> iter) {
        find(ReadOnlyIterator.of(list), iter);
    }

    /**
     * Linear iteration of the given interval until iteration exhausts all elements
     */
    public default <T> void iterate(ReadOnlyIterator<T> iterator, Iter.IterNoStop<T> iter) {
        find(iterator, iter);
    }

    /**
     * Linear iteration of the given interval until iteration exhausts all elements
     */
    public default <T> void iterate(Stream<T> stream, Iter.IterNoStop<T> iter) {
        find(ReadOnlyIterator.of(stream), iter);
    }

}
