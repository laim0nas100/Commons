package lt.lb.commons.iteration.general;

import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;
import lt.lb.commons.SafeOpt;
import lt.lb.commons.iteration.ReadOnlyIterator;
import lt.lb.commons.iteration.general.cons.IterIterableBiCons;
import lt.lb.commons.iteration.general.cons.IterIterableBiConsNoStop;
import lt.lb.commons.iteration.general.cons.IterIterableCons;
import lt.lb.commons.iteration.general.cons.IterIterableConsNoStop;
import lt.lb.commons.iteration.general.result.IterIterableResult;

/**
 *
 * @author laim0nas100
 * @param <E>
 */
public interface IterationIterable<E extends IterationIterable<E>> extends IterationAbstract<E> {

    /**
     * Linear iteration of the given interval until logic returns true, or
     * iteration exhausts all elements
     */
    public <T> SafeOpt<IterIterableResult<T>> find(Iterator<T> iterator, IterIterableCons<T> iter);

    /**
     * Linear iteration of the given interval until logic returns true, or
     * iteration exhausts all elements
     */
    public <T> SafeOpt<IterIterableResult<T>> find(List<T> list, IterIterableCons<T> iter);

    /**
     * Linear iteration of the given interval until logic returns true, or
     * iteration exhausts all elements
     */
    public <T> SafeOpt<IterIterableResult<T>> find(T[] array, IterIterableCons<T> iter);

    /**
     * Linear backward iteration of the given interval until logic returns true,
     * or iteration exhausts all elements
     */
    public <T> SafeOpt<IterIterableResult<T>> findBackwards(List<T> list, IterIterableCons<T> iter);

    /**
     * Linear backward iteration of the given interval until logic returns true,
     * or iteration exhausts all elements
     */
    public <T> SafeOpt<IterIterableResult<T>> findBackwards(Deque<T> deque, IterIterableCons<T> iter);

    /**
     * Linear backward iteration of the given interval until iteration exhausts
     * all elements
     */
    public default <T> SafeOpt<Void> findBackwards(LinkedList<T> list, IterIterableConsNoStop<T> iter) {
        return findBackwards((List<T>) list, iter).keepError();

    }

    /**
     * Linear backward iteration of the given interval until logic returns true,
     * or iteration exhausts all elements
     */
    public <T> SafeOpt<IterIterableResult<T>> findBackwards(T[] array, IterIterableCons<T> iter);

    /**
     * Linear backward iteration of the given interval until iteration exhausts
     * all elements
     */
    public default <T> SafeOpt<Void> iterateBackwards(LinkedList<T> list, IterIterableConsNoStop<T> iter) {
        Objects.requireNonNull(list, "List is null");
        Objects.requireNonNull(iter, "Iteration logic is null");
        return findBackwards((List<T>) list, iter).keepError();
    }

    /**
     * Linear backward iteration of the given interval until iteration exhausts
     * all elements
     */
    public default <T> SafeOpt<Void> iterateBackwards(List<T> list, IterIterableConsNoStop<T> iter) {
        Objects.requireNonNull(list, "List is null");
        Objects.requireNonNull(iter, "Iteration logic is null");
        return findBackwards(list, iter).keepError();
    }

    /**
     * Linear backward iteration of the given interval until iteration exhausts
     * all elements
     */
    public default <T> SafeOpt<Void> iterateBackwards(Deque<T> deque, IterIterableConsNoStop<T> iter) {
        Objects.requireNonNull(deque, "Deque is null");
        Objects.requireNonNull(iter, "Iteration logic is null");
        return findBackwards(deque, iter).keepError();
    }

    /**
     * Linear backward iteration of the given interval until iteration exhausts
     * all elements
     */
    public default <T> SafeOpt<Void> iterateBackwards(T[] array, IterIterableConsNoStop<T> iter) {
        Objects.requireNonNull(array, "Array is null");
        Objects.requireNonNull(iter, "Iteration logic is null");
        return findBackwards(array, iter).keepError();
    }

    /**
     * Linear iteration of the given interval until logic returns true, or
     * iteration exhausts all elements
     */
    public default <T> SafeOpt<IterIterableResult<T>> find(Iterable<T> iterable, IterIterableCons<T> iter) {
        Objects.requireNonNull(iterable, "Iterable is null");
        Objects.requireNonNull(iter, "Iteration logic is null");
        return find(iterable.iterator(), iter);
    }

    /**
     * Linear iteration of the given interval until logic returns true, or
     * iteration exhausts all elements
     */
    public default <T> SafeOpt<IterIterableResult<T>> find(ReadOnlyIterator<T> iterator, IterIterableCons<T> iter) {
        Objects.requireNonNull(iterator, "ReadOnlyIterator is null");
        Objects.requireNonNull(iter, "Iteration logic is null");
        return find((Iterator) iterator, iter);
    }

    /**
     * Linear iteration of the given interval until logic returns true, or
     * iteration exhausts all elements
     */
    public default <T> SafeOpt<IterIterableResult<T>> find(Stream<T> stream, IterIterableCons<T> iter) {
        Objects.requireNonNull(stream, "Stream is null");
        Objects.requireNonNull(iter, "Iteration logic is null");
        return find(ReadOnlyIterator.of(stream), iter);
    }

    /**
     * Linear iteration of the given interval until iteration exhausts all
     * elements
     */
    public default <T> SafeOpt<Void> iterate(List<T> list, IterIterableConsNoStop<T> iter) {
        Objects.requireNonNull(list, "List is null");
        Objects.requireNonNull(iter, "Iteration logic is null");
        return find(list, iter).keepError();
    }

    /**
     * Linear iteration of the given interval until iteration exhausts all
     * elements
     */
    public default <T> SafeOpt<Void> iterate(T[] array, IterIterableConsNoStop<T> iter) {
        Objects.requireNonNull(array, "Array is null");
        Objects.requireNonNull(iter, "Iteration logic is null");
        return find(array, iter).keepError();
    }

    /**
     * Linear iteration of the given interval until iteration exhausts all
     * elements
     */
    public default <T> SafeOpt<Void> iterate(Iterator<T> iterator, IterIterableConsNoStop<T> iter) {
        Objects.requireNonNull(iterator, "Iterator is null");
        Objects.requireNonNull(iter, "Iteration logic is null");
        return find(iterator, iter).keepError();
    }

    /**
     * Linear iteration of the given interval until iteration exhausts all
     * elements
     */
    public default <T> SafeOpt<Void> iterate(Iterable<T> iterable, IterIterableConsNoStop<T> iter) {
        Objects.requireNonNull(iterable, "Iterable is null");
        Objects.requireNonNull(iter, "Iteration logic is null");
        return find(iterable.iterator(), iter).keepError();
    }

    /**
     * Linear iteration of the given interval until iteration exhausts all
     * elements
     */
    public default <T> SafeOpt<Void> iterate(ReadOnlyIterator<T> iterator, IterIterableConsNoStop<T> iter) {
        Objects.requireNonNull(iterator, "ReadOnlyIterator is null");
        Objects.requireNonNull(iter, "Iteration logic is null");
        return find(iterator, iter).keepError();
    }

    /**
     * Linear iteration of the given interval until iteration exhausts all
     * elements
     */
    public default <T> SafeOpt<Void> iterate(Stream<T> stream, IterIterableConsNoStop<T> iter) {
        Objects.requireNonNull(stream, "Stream is null");
        Objects.requireNonNull(iter, "Iteration logic is null");
        return find(stream.iterator(), iter).keepError();
    }

    /**
     * Linear iteration of the given interval until logic returns true, or
     * iteration exhausts all elements
     */
    public default <T> SafeOpt<IterIterableResult<T>> find(ReadOnlyIterator<T> iterator, IterIterableBiCons<T> iter) {
        Objects.requireNonNull(iterator, "ReadOnlyIterator is null");
        Objects.requireNonNull(iter, "Iteration logic is null");
        return find(iterator, (IterIterableCons<T>) iter);
    }

    /**
     * Linear iteration of the given interval until logic returns true, or
     * iteration exhausts all elements
     */
    public default <T> SafeOpt<IterIterableResult<T>> find(List<T> list, IterIterableBiCons<T> iter) {
        Objects.requireNonNull(list, "List is null");
        Objects.requireNonNull(iter, "Iteration logic is null");
        return find(list, (IterIterableCons<T>) iter);
    }

    /**
     * Linear iteration of the given interval until logic returns true, or
     * iteration exhausts all elements
     */
    public default <T> SafeOpt<IterIterableResult<T>> find(T[] array, IterIterableBiCons<T> iter) {
        Objects.requireNonNull(array, "Array is null");
        Objects.requireNonNull(iter, "Iteration logic is null");
        return find(array, (IterIterableCons<T>) iter);
    }

    /**
     * Linear backward iteration of the given interval until logic returns true,
     * or iteration exhausts all elements
     */
    public default <T> SafeOpt<IterIterableResult<T>> findBackwards(List<T> list, IterIterableBiCons<T> iter) {
        Objects.requireNonNull(list, "List is null");
        Objects.requireNonNull(iter, "Iteration logic is null");
        return findBackwards(list, (IterIterableCons<T>) iter);
    }

    /**
     * Linear backward iteration of the given interval until logic returns true,
     * or iteration exhausts all elements
     */
    public default <T> SafeOpt<IterIterableResult<T>> findBackwards(Deque<T> deque, IterIterableBiCons<T> iter) {
        Objects.requireNonNull(deque, "Deque is null");
        Objects.requireNonNull(iter, "Iteration logic is null");
        return findBackwards(deque, (IterIterableCons<T>) iter);
    }

    /**
     * Linear backward iteration of the given interval until iteration exhausts
     * all elements
     */
    public default <T> SafeOpt<IterIterableResult<T>> findBackwards(LinkedList<T> list, IterIterableBiConsNoStop<T> iter) {
        Objects.requireNonNull(list, "LinkedList is null");
        Objects.requireNonNull(iter, "Iteration logic is null");
        return findBackwards((List<T>) list, iter);
    }

    /**
     * Linear backward iteration of the given interval until logic returns true,
     * or iteration exhausts all elements
     */
    public default <T> SafeOpt<IterIterableResult<T>> findBackwards(T[] array, IterIterableBiCons<T> iter) {
        Objects.requireNonNull(array, "Array is null");
        Objects.requireNonNull(iter, "Iteration logic is null");
        return findBackwards(array, (IterIterableCons<T>) iter);
    }

    /**
     * Linear backward iteration of the given interval until iteration exhausts
     * all elements
     */
    public default <T> SafeOpt<Void> iterateBackwards(LinkedList<T> list, IterIterableBiConsNoStop<T> iter) {
        Objects.requireNonNull(list, "LinkedList is null");
        Objects.requireNonNull(iter, "Iteration logic is null");
        return findBackwards((List<T>) list, iter).keepError();
    }

    /**
     * Linear backward iteration of the given interval until iteration exhausts
     * all elements
     */
    public default <T> SafeOpt<Void> iterateBackwards(List<T> list, IterIterableBiConsNoStop<T> iter) {
        Objects.requireNonNull(list, "List is null");
        Objects.requireNonNull(iter, "Iteration logic is null");
        return findBackwards(list, iter).keepError();
    }

    /**
     * Linear backward iteration of the given interval until iteration exhausts
     * all elements
     */
    public default <T> SafeOpt<Void> iterateBackwards(Deque<T> deque, IterIterableBiConsNoStop<T> iter) {
        Objects.requireNonNull(deque, "Deque is null");
        Objects.requireNonNull(iter, "Iteration logic is null");
        return findBackwards(deque, iter).keepError();
    }

    /**
     * Linear backward iteration of the given interval until iteration exhausts
     * all elements
     */
    public default <T> SafeOpt<Void> iterateBackwards(T[] array, IterIterableBiConsNoStop<T> iter) {
        Objects.requireNonNull(array, "Array is null");
        Objects.requireNonNull(iter, "Iteration logic is null");
        return findBackwards(array, iter).keepError();
    }

    /**
     * Linear iteration of the given interval until logic returns true, or
     * iteration exhausts all elements
     */
    public default <T> SafeOpt<IterIterableResult<T>> find(Iterable<T> iterable, IterIterableBiCons<T> iter) {
        Objects.requireNonNull(iterable, "Iterable is null");
        Objects.requireNonNull(iter, "Iteration logic is null");
        return find(iterable.iterator(), iter);
    }

    /**
     * Linear iteration of the given interval until logic returns true, or
     * iteration exhausts all elements
     */
    public default <T> SafeOpt<IterIterableResult<T>> find(Iterator<T> iterator, IterIterableBiCons<T> iter) {
        Objects.requireNonNull(iterator, "Iterator is null");
        Objects.requireNonNull(iter, "Iteration logic is null");
        return find(iterator, (IterIterableCons<T>) iter);
    }

    /**
     * Linear iteration of the given interval until logic returns true, or
     * iteration exhausts all elements
     */
    public default <T> SafeOpt<IterIterableResult<T>> find(Stream<T> stream, IterIterableBiCons<T> iter) {
        Objects.requireNonNull(stream, "Stream is null");
        Objects.requireNonNull(iter, "Iteration logic is null");
        return find(stream.iterator(), iter);
    }

    /**
     * Linear iteration of the given interval until iteration exhausts all
     * elements
     */
    public default <T> SafeOpt<Void> iterate(List<T> list, IterIterableBiConsNoStop<T> iter) {
        Objects.requireNonNull(list, "List is null");
        Objects.requireNonNull(iter, "Iteration logic is null");
        return find(list, iter).keepError();
    }

    /**
     * Linear iteration of the given interval until iteration exhausts all
     * elements
     */
    public default <T> SafeOpt<Void> iterate(T[] array, IterIterableBiConsNoStop<T> iter) {
        Objects.requireNonNull(array, "Array is null");
        Objects.requireNonNull(iter, "Iteration logic is null");
        return find(array, iter).keepError();
    }

    /**
     * Linear iteration of the given interval until iteration exhausts all
     * elements
     */
    public default <T> SafeOpt<Void> iterate(Iterator<T> iterator, IterIterableBiConsNoStop<T> iter) {
        Objects.requireNonNull(iterator, "Iterator is null");
        Objects.requireNonNull(iter, "Iteration logic is null");
        return find(iterator, iter).keepError();
    }

    /**
     * Linear iteration of the given interval until iteration exhausts all
     * elements
     */
    public default <T> SafeOpt<Void> iterate(Iterable<T> iterable, IterIterableBiConsNoStop<T> iter) {
        Objects.requireNonNull(iterable, "Iterable is null");
        Objects.requireNonNull(iter, "Iteration logic is null");
        return find(iterable.iterator(), iter).keepError();
    }

    /**
     * Linear iteration of the given interval until iteration exhausts all
     * elements
     */
    public default <T> SafeOpt<Void> iterate(ReadOnlyIterator<T> iterator, IterIterableBiConsNoStop<T> iter) {
        Objects.requireNonNull(iterator, "ReadOnlyIterator is null");
        Objects.requireNonNull(iter, "Iteration logic is null");
        return find(iterator, iter).keepError();
    }

    /**
     * Linear iteration of the given interval until iteration exhausts all
     * elements
     */
    public default <T> SafeOpt<Void> iterate(Stream<T> stream, IterIterableBiConsNoStop<T> iter) {
        Objects.requireNonNull(stream, "Stream is null");
        Objects.requireNonNull(iter, "Iteration logic is null");
        return find(stream.iterator(), iter).keepError();
    }

}
