package lt.lb.commons.iteration.general;

import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import lt.lb.commons.iteration.ReadOnlyIterator;
import lt.lb.commons.iteration.general.cons.IterIterableBiCons;
import lt.lb.commons.iteration.general.cons.IterIterableCons;
import lt.lb.commons.iteration.general.result.IterIterableResult;

/**
 *
 * @author laim0nas100
 * @param <E>
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
     * Linear iteration of the given interval until logic returns true, or
     * iteration exhausts all elements
     */
    public <T> Optional<IterIterableResult<T>> find(Iterator<T> iterator, IterIterableCons<T> iter);

    /**
     * Linear iteration of the given interval until logic returns true, or
     * iteration exhausts all elements
     */
    public <T> Optional<IterIterableResult<T>> find(List<T> list, IterIterableCons<T> iter);

    /**
     * Linear iteration of the given interval until logic returns true, or
     * iteration exhausts all elements
     */
    public <T> Optional<IterIterableResult<T>> find(T[] array, IterIterableCons<T> iter);

    /**
     * Linear backward iteration of the given interval until logic returns true,
     * or iteration exhausts all elements
     */
    public <T> Optional<IterIterableResult<T>> findBackwards(List<T> list, IterIterableCons<T> iter);

    /**
     * Linear backward iteration of the given interval until logic returns true,
     * or iteration exhausts all elements
     */
    public <T> Optional<IterIterableResult<T>> findBackwards(Deque<T> deque, IterIterableCons<T> iter);

    /**
     * Linear backward iteration of the given interval until iteration exhausts
     * all elements
     */
    public default <T> void findBackwards(LinkedList<T> list, IterIterableCons.IterIterableConsNoStop<T> iter) {
        findBackwards((List<T>) list, iter);
    }

    /**
     * Linear backward iteration of the given interval until logic returns true,
     * or iteration exhausts all elements
     */
    public <T> Optional<IterIterableResult<T>> findBackwards(T[] array, IterIterableCons<T> iter);

    /**
     * Linear backward iteration of the given interval until iteration exhausts
     * all elements
     */
    public default <T> void iterateBackwards(LinkedList<T> list, IterIterableCons.IterIterableConsNoStop<T> iter) {
        findBackwards((List<T>) list, iter);
    }

    /**
     * Linear backward iteration of the given interval until iteration exhausts
     * all elements
     */
    public default <T> void iterateBackwards(List<T> list, IterIterableCons.IterIterableConsNoStop<T> iter) {
        findBackwards(list, iter);
    }

    /**
     * Linear backward iteration of the given interval until iteration exhausts
     * all elements
     */
    public default <T> void iterateBackwards(Deque<T> deque, IterIterableCons.IterIterableConsNoStop<T> iter) {
        findBackwards(deque, iter);
    }

    /**
     * Linear backward iteration of the given interval until iteration exhausts
     * all elements
     */
    public default <T> void iterateBackwards(T[] array, IterIterableCons.IterIterableConsNoStop<T> iter) {
        findBackwards(array, iter);
    }

    /**
     * Linear iteration of the given interval until logic returns true, or
     * iteration exhausts all elements
     */
    public default <T> Optional<IterIterableResult<T>> find(Iterable<T> iterable, IterIterableCons<T> iter) {
        return find(iterable.iterator(), iter);
    }

    /**
     * Linear iteration of the given interval until logic returns true, or
     * iteration exhausts all elements
     */
    public default <T> Optional<IterIterableResult<T>> find(ReadOnlyIterator<T> iterator, IterIterableCons<T> iter) {
        return find((Iterator) iterator, iter);
    }

    /**
     * Linear iteration of the given interval until logic returns true, or
     * iteration exhausts all elements
     */
    public default <T> Optional<IterIterableResult<T>> find(Stream<T> stream, IterIterableCons<T> iter) {
        return find(ReadOnlyIterator.of(stream), iter);
    }

    /**
     * Linear iteration of the given interval until iteration exhausts all
     * elements
     */
    public default <T> void iterate(List<T> list, IterIterableCons.IterIterableConsNoStop<T> iter) {
        find(list, iter);
    }

    /**
     * Linear iteration of the given interval until iteration exhausts all
     * elements
     */
    public default <T> void iterate(T[] array, IterIterableCons.IterIterableConsNoStop<T> iter) {
        find(array, iter);
    }

    /**
     * Linear iteration of the given interval until iteration exhausts all
     * elements
     */
    public default <T> void iterate(Iterator<T> iterator, IterIterableCons.IterIterableConsNoStop<T> iter) {
        find(iterator, iter);
    }

    /**
     * Linear iteration of the given interval until iteration exhausts all
     * elements
     */
    public default <T> void iterate(Iterable<T> iterable, IterIterableCons.IterIterableConsNoStop<T> iter) {
        find(iterable.iterator(), iter);
    }

    /**
     * Linear iteration of the given interval until iteration exhausts all
     * elements
     */
    public default <T> void iterate(ReadOnlyIterator<T> iterator, IterIterableCons.IterIterableConsNoStop<T> iter) {
        find(iterator, iter);
    }

    /**
     * Linear iteration of the given interval until iteration exhausts all
     * elements
     */
    public default <T> void iterate(Stream<T> stream, IterIterableCons.IterIterableConsNoStop<T> iter) {
        find(stream.iterator(), iter);
    }

    /**
     * Linear iteration of the given interval until logic returns true, or
     * iteration exhausts all elements
     */
    public default <T> Optional<IterIterableResult<T>> find(ReadOnlyIterator<T> iterator, IterIterableBiCons<T> iter) {
        return find(iterator, (IterIterableCons<T>) iter);
    }

    /**
     * Linear iteration of the given interval until logic returns true, or
     * iteration exhausts all elements
     */
    public default <T> Optional<IterIterableResult<T>> find(List<T> list, IterIterableBiCons<T> iter) {
        return find(list, (IterIterableCons<T>) iter);
    }

    /**
     * Linear iteration of the given interval until logic returns true, or
     * iteration exhausts all elements
     */
    public default <T> Optional<IterIterableResult<T>> find(T[] array, IterIterableBiCons<T> iter) {
        return find(array, (IterIterableCons<T>) iter);
    }

    /**
     * Linear backward iteration of the given interval until logic returns true,
     * or iteration exhausts all elements
     */
    public default <T> Optional<IterIterableResult<T>> findBackwards(List<T> list, IterIterableBiCons<T> iter) {
        return findBackwards(list, (IterIterableCons<T>) iter);
    }

    /**
     * Linear backward iteration of the given interval until logic returns true,
     * or iteration exhausts all elements
     */
    public default <T> Optional<IterIterableResult<T>> findBackwards(Deque<T> deque, IterIterableBiCons<T> iter) {
        return findBackwards(deque, (IterIterableCons<T>) iter);
    }

    /**
     * Linear backward iteration of the given interval until iteration exhausts
     * all elements
     */
    public default <T> Optional<IterIterableResult<T>> findBackwards(LinkedList<T> list, IterIterableBiCons.IterIterableBiConsNoStop<T> iter) {
        return findBackwards((List<T>) list, iter);
    }

    /**
     * Linear backward iteration of the given interval until logic returns true,
     * or iteration exhausts all elements
     */
    public default <T> Optional<IterIterableResult<T>> findBackwards(T[] array, IterIterableBiCons<T> iter) {
        return findBackwards(array, (IterIterableCons<T>) iter);
    }

    /**
     * Linear backward iteration of the given interval until iteration exhausts
     * all elements
     */
    public default <T> void iterateBackwards(LinkedList<T> list, IterIterableBiCons.IterIterableBiConsNoStop<T> iter) {
        findBackwards((List<T>) list, iter);
    }

    /**
     * Linear backward iteration of the given interval until iteration exhausts
     * all elements
     */
    public default <T> void iterateBackwards(List<T> list, IterIterableBiCons.IterIterableBiConsNoStop<T> iter) {
        findBackwards(list, iter);
    }

    /**
     * Linear backward iteration of the given interval until iteration exhausts
     * all elements
     */
    public default <T> void iterateBackwards(Deque<T> deque, IterIterableBiCons.IterIterableBiConsNoStop<T> iter) {
        findBackwards(deque, iter);
    }

    /**
     * Linear backward iteration of the given interval until iteration exhausts
     * all elements
     */
    public default <T> void iterateBackwards(T[] array, IterIterableBiCons.IterIterableBiConsNoStop<T> iter) {
        findBackwards(array, iter);
    }

    /**
     * Linear iteration of the given interval until logic returns true, or
     * iteration exhausts all elements
     */
    public default <T> Optional<IterIterableResult<T>> find(Iterable<T> iterable, IterIterableBiCons<T> iter) {
        return find(iterable.iterator(), iter);
    }

    /**
     * Linear iteration of the given interval until logic returns true, or
     * iteration exhausts all elements
     */
    public default <T> Optional<IterIterableResult<T>> find(Iterator<T> iterator, IterIterableBiCons<T> iter) {
        return find(iterator, (IterIterableCons<T>) iter);
    }

    /**
     * Linear iteration of the given interval until logic returns true, or
     * iteration exhausts all elements
     */
    public default <T> Optional<IterIterableResult<T>> find(Stream<T> stream, IterIterableBiCons<T> iter) {
        return find(stream.iterator(), iter);
    }

    /**
     * Linear iteration of the given interval until iteration exhausts all
     * elements
     */
    public default <T> void iterate(List<T> list, IterIterableBiCons.IterIterableBiConsNoStop<T> iter) {
        find(list, iter);
    }

    /**
     * Linear iteration of the given interval until iteration exhausts all
     * elements
     */
    public default <T> void iterate(T[] array, IterIterableBiCons.IterIterableBiConsNoStop<T> iter) {
        find(array, iter);
    }

    /**
     * Linear iteration of the given interval until iteration exhausts all
     * elements
     */
    public default <T> void iterate(Iterator<T> iterator, IterIterableBiCons.IterIterableBiConsNoStop<T> iter) {
        find(iterator, iter);
    }

    /**
     * Linear iteration of the given interval until iteration exhausts all
     * elements
     */
    public default <T> void iterate(Iterable<T> iterable, IterIterableBiCons.IterIterableBiConsNoStop<T> iter) {
        find(iterable.iterator(), iter);
    }

    /**
     * Linear iteration of the given interval until iteration exhausts all
     * elements
     */
    public default <T> void iterate(ReadOnlyIterator<T> iterator, IterIterableBiCons.IterIterableBiConsNoStop<T> iter) {
        find(iterator, iter);
    }

    /**
     * Linear iteration of the given interval until iteration exhausts all
     * elements
     */
    public default <T> void iterate(Stream<T> stream, IterIterableBiCons.IterIterableBiConsNoStop<T> iter) {
        find(stream.iterator(), iter);
    }

}
