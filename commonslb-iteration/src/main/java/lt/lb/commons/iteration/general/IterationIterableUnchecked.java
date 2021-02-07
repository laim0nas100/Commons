package lt.lb.commons.iteration.general;

import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;
import lt.lb.commons.SafeOpt;
import lt.lb.commons.iteration.ReadOnlyIterator;
import lt.lb.commons.iteration.general.cons.unchecked.IterIterableBiConsNoStopUnchecked;
import lt.lb.commons.iteration.general.cons.unchecked.IterIterableBiConsUnchecked;
import lt.lb.commons.iteration.general.cons.unchecked.IterIterableConsNoStopUnchecked;
import lt.lb.commons.iteration.general.cons.unchecked.IterIterableConsUnchecked;
import lt.lb.commons.iteration.general.result.IterIterableResult;

/**
 *
 * @author laim0nas100
 * @param <E>
 */
public interface IterationIterableUnchecked<E extends IterationIterableUnchecked<E>> extends IterationAbstract<E> {

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
    public <T> SafeOpt<IterIterableResult<T>> find(Iterator<T> iterator, IterIterableConsUnchecked<T> iter);

    /**
     * Linear iteration of the given interval until logic returns true, or
     * iteration exhausts all elements
     */
    public <T> SafeOpt<IterIterableResult<T>> find(List<T> list, IterIterableConsUnchecked<T> iter);

    /**
     * Linear iteration of the given interval until logic returns true, or
     * iteration exhausts all elements
     */
    public <T> SafeOpt<IterIterableResult<T>> find(T[] array, IterIterableConsUnchecked<T> iter);

    /**
     * Linear backward iteration of the given interval until logic returns true,
     * or iteration exhausts all elements
     */
    public <T> SafeOpt<IterIterableResult<T>> findBackwards(List<T> list, IterIterableConsUnchecked<T> iter);

    /**
     * Linear backward iteration of the given interval until logic returns true,
     * or iteration exhausts all elements
     */
    public <T> SafeOpt<IterIterableResult<T>> findBackwards(Deque<T> deque, IterIterableConsUnchecked<T> iter);

    /**
     * Linear backward iteration of the given interval until iteration exhausts
     * all elements
     */
    public default <T> SafeOpt<Void> findBackwards(LinkedList<T> list, IterIterableConsNoStopUnchecked<T> iter) {
        return findBackwards((List<T>) list, iter).keepError();

    }

    /**
     * Linear backward iteration of the given interval until logic returns true,
     * or iteration exhausts all elements
     */
    public <T> SafeOpt<IterIterableResult<T>> findBackwards(T[] array, IterIterableConsUnchecked<T> iter);

    /**
     * Linear backward iteration of the given interval until iteration exhausts
     * all elements
     */
    public default <T> SafeOpt<Void> iterateBackwards(LinkedList<T> list, IterIterableConsNoStopUnchecked<T> iter) {
        return findBackwards((List<T>) list, iter).keepError();
    }

    /**
     * Linear backward iteration of the given interval until iteration exhausts
     * all elements
     */
    public default <T> SafeOpt<Void> iterateBackwards(List<T> list, IterIterableConsNoStopUnchecked<T> iter) {
        return findBackwards(list, iter).keepError();
    }

    /**
     * Linear backward iteration of the given interval until iteration exhausts
     * all elements
     */
    public default <T> SafeOpt<Void> iterateBackwards(Deque<T> deque, IterIterableConsNoStopUnchecked<T> iter) {
        return findBackwards(deque, iter).keepError();
    }

    /**
     * Linear backward iteration of the given interval until iteration exhausts
     * all elements
     */
    public default <T> SafeOpt<Void> iterateBackwards(T[] array, IterIterableConsNoStopUnchecked<T> iter) {
        return findBackwards(array, iter).keepError();
    }

    /**
     * Linear iteration of the given interval until logic returns true, or
     * iteration exhausts all elements
     */
    public default <T> SafeOpt<IterIterableResult<T>> find(Iterable<T> iterable, IterIterableConsUnchecked<T> iter) {
        return find(iterable.iterator(), iter);
    }

    /**
     * Linear iteration of the given interval until logic returns true, or
     * iteration exhausts all elements
     */
    public default <T> SafeOpt<IterIterableResult<T>> find(ReadOnlyIterator<T> iterator, IterIterableConsUnchecked<T> iter) {
        return find((Iterator) iterator, iter);
    }

    /**
     * Linear iteration of the given interval until logic returns true, or
     * iteration exhausts all elements
     */
    public default <T> SafeOpt<IterIterableResult<T>> find(Stream<T> stream, IterIterableConsUnchecked<T> iter) {
        return find(ReadOnlyIterator.of(stream), iter);
    }

    /**
     * Linear iteration of the given interval until iteration exhausts all
     * elements
     */
    public default <T> SafeOpt<Void> iterate(List<T> list, IterIterableConsNoStopUnchecked<T> iter) {
        return find(list, iter).keepError();
    }

    /**
     * Linear iteration of the given interval until iteration exhausts all
     * elements
     */
    public default <T> SafeOpt<Void> iterate(T[] array, IterIterableConsNoStopUnchecked<T> iter) {
        return find(array, iter).keepError();
    }

    /**
     * Linear iteration of the given interval until iteration exhausts all
     * elements
     */
    public default <T> SafeOpt<Void> iterate(Iterator<T> iterator, IterIterableConsNoStopUnchecked<T> iter) {
        return find(iterator, iter).keepError();
    }

    /**
     * Linear iteration of the given interval until iteration exhausts all
     * elements
     */
    public default <T> SafeOpt<Void> iterate(Iterable<T> iterable, IterIterableConsNoStopUnchecked<T> iter) {
        return find(iterable.iterator(), iter).keepError();
    }

    /**
     * Linear iteration of the given interval until iteration exhausts all
     * elements
     */
    public default <T> SafeOpt<Void> iterate(ReadOnlyIterator<T> iterator, IterIterableConsNoStopUnchecked<T> iter) {
        return find(iterator, iter).keepError();
    }

    /**
     * Linear iteration of the given interval until iteration exhausts all
     * elements
     */
    public default <T> SafeOpt<Void> iterate(Stream<T> stream, IterIterableConsNoStopUnchecked<T> iter) {
        return find(stream.iterator(), iter).keepError();
    }

    /**
     * Linear iteration of the given interval until logic returns true, or
     * iteration exhausts all elements
     */
    public default <T> SafeOpt<IterIterableResult<T>> find(ReadOnlyIterator<T> iterator, IterIterableBiConsUnchecked<T> iter) {
        return find(iterator, (IterIterableConsUnchecked<T>) iter);
    }

    /**
     * Linear iteration of the given interval until logic returns true, or
     * iteration exhausts all elements
     */
    public default <T> SafeOpt<IterIterableResult<T>> find(List<T> list, IterIterableBiConsUnchecked<T> iter) {
        return find(list, (IterIterableConsUnchecked<T>) iter);
    }

    /**
     * Linear iteration of the given interval until logic returns true, or
     * iteration exhausts all elements
     */
    public default <T> SafeOpt<IterIterableResult<T>> find(T[] array, IterIterableBiConsUnchecked<T> iter) {
        return find(array, (IterIterableConsUnchecked<T>) iter);
    }

    /**
     * Linear backward iteration of the given interval until logic returns true,
     * or iteration exhausts all elements
     */
    public default <T> SafeOpt<IterIterableResult<T>> findBackwards(List<T> list, IterIterableBiConsUnchecked<T> iter) {
        return findBackwards(list, (IterIterableConsUnchecked<T>) iter);
    }

    /**
     * Linear backward iteration of the given interval until logic returns true,
     * or iteration exhausts all elements
     */
    public default <T> SafeOpt<IterIterableResult<T>> findBackwards(Deque<T> deque, IterIterableBiConsUnchecked<T> iter) {
        return findBackwards(deque, (IterIterableConsUnchecked<T>) iter);
    }

    /**
     * Linear backward iteration of the given interval until iteration exhausts
     * all elements
     */
    public default <T> SafeOpt<IterIterableResult<T>> findBackwards(LinkedList<T> list, IterIterableBiConsNoStopUnchecked<T> iter) {
        return findBackwards((List<T>) list, iter);
    }

    /**
     * Linear backward iteration of the given interval until logic returns true,
     * or iteration exhausts all elements
     */
    public default <T> SafeOpt<IterIterableResult<T>> findBackwards(T[] array, IterIterableBiConsUnchecked<T> iter) {
        return findBackwards(array, (IterIterableConsUnchecked<T>) iter);
    }

    /**
     * Linear backward iteration of the given interval until iteration exhausts
     * all elements
     */
    public default <T> SafeOpt<Void> iterateBackwards(LinkedList<T> list, IterIterableBiConsNoStopUnchecked<T> iter) {
        return findBackwards((List<T>) list, iter).keepError();
    }

    /**
     * Linear backward iteration of the given interval until iteration exhausts
     * all elements
     */
    public default <T> SafeOpt<Void> iterateBackwards(List<T> list, IterIterableBiConsNoStopUnchecked<T> iter) {
        return findBackwards(list, iter).keepError();
    }

    /**
     * Linear backward iteration of the given interval until iteration exhausts
     * all elements
     */
    public default <T> SafeOpt<Void> iterateBackwards(Deque<T> deque, IterIterableBiConsNoStopUnchecked<T> iter) {
        return findBackwards(deque, iter).keepError();
    }

    /**
     * Linear backward iteration of the given interval until iteration exhausts
     * all elements
     */
    public default <T> SafeOpt<Void> iterateBackwards(T[] array, IterIterableBiConsNoStopUnchecked<T> iter) {
        return findBackwards(array, iter).keepError();
    }

    /**
     * Linear iteration of the given interval until logic returns true, or
     * iteration exhausts all elements
     */
    public default <T> SafeOpt<IterIterableResult<T>> find(Iterable<T> iterable, IterIterableBiConsUnchecked<T> iter) {
        return find(iterable.iterator(), iter);
    }

    /**
     * Linear iteration of the given interval until logic returns true, or
     * iteration exhausts all elements
     */
    public default <T> SafeOpt<IterIterableResult<T>> find(Iterator<T> iterator, IterIterableBiConsUnchecked<T> iter) {
        return find(iterator, (IterIterableConsUnchecked<T>) iter);
    }

    /**
     * Linear iteration of the given interval until logic returns true, or
     * iteration exhausts all elements
     */
    public default <T> SafeOpt<IterIterableResult<T>> find(Stream<T> stream, IterIterableBiConsUnchecked<T> iter) {
        return find(stream.iterator(), iter);
    }

    /**
     * Linear iteration of the given interval until iteration exhausts all
     * elements
     */
    public default <T> SafeOpt<Void> iterate(List<T> list, IterIterableBiConsNoStopUnchecked<T> iter) {
        return find(list, iter).keepError();
    }

    /**
     * Linear iteration of the given interval until iteration exhausts all
     * elements
     */
    public default <T> SafeOpt<Void> iterate(T[] array, IterIterableBiConsNoStopUnchecked<T> iter) {
        return find(array, iter).keepError();
    }

    /**
     * Linear iteration of the given interval until iteration exhausts all
     * elements
     */
    public default <T> SafeOpt<Void> iterate(Iterator<T> iterator, IterIterableBiConsNoStopUnchecked<T> iter) {
        return find(iterator, iter).keepError();
    }

    /**
     * Linear iteration of the given interval until iteration exhausts all
     * elements
     */
    public default <T> SafeOpt<Void> iterate(Iterable<T> iterable, IterIterableBiConsNoStopUnchecked<T> iter) {
        return find(iterable.iterator(), iter).keepError();
    }

    /**
     * Linear iteration of the given interval until iteration exhausts all
     * elements
     */
    public default <T> SafeOpt<Void> iterate(ReadOnlyIterator<T> iterator, IterIterableBiConsNoStopUnchecked<T> iter) {
        return find(iterator, iter).keepError();
    }

    /**
     * Linear iteration of the given interval until iteration exhausts all
     * elements
     */
    public default <T> SafeOpt<Void> iterate(Stream<T> stream, IterIterableBiConsNoStopUnchecked<T> iter) {
        return find(stream.iterator(), iter).keepError();
    }

}
