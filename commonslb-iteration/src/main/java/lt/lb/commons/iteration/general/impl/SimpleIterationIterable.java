package lt.lb.commons.iteration.general.impl;

import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;
import lt.lb.commons.SafeOpt;
import lt.lb.commons.iteration.general.IterationIterable;
import lt.lb.commons.iteration.general.accessors.IterIterableAccessor;
import lt.lb.commons.iteration.general.cons.IterIterableCons;
import lt.lb.commons.iteration.general.result.IterIterableResult;

/**
 *
 * @author laim0nas100
 */
public class SimpleIterationIterable extends SimpleAbstractIteration<SimpleIterationIterable> implements IterationIterable<SimpleIterationIterable> {

    @Override
    public <T> SafeOpt<IterIterableResult<T>> findBackwards(List<T> list, IterIterableCons<T> iter) {
        Objects.requireNonNull(list, "List is null");
        Objects.requireNonNull(iter, "Iteration logic is null");
        int[] bound = workoutBounds(list.size());
        int from = bound[0];
        int to = bound[1];
        ListIterator<T> iterator = list.listIterator(to);
        to--;
        IterIterableAccessor accessor = resolveAccessor(iter);
        while (iterator.hasPrevious() && from <= to) {
            T next = iterator.previous();

            SafeOpt<IterIterableResult<T>> tryVisit = accessor.tryVisit(to, next, iter);
            if (tryVisit.hasValueOrError()) {
                return tryVisit;
            }
            to--;
        }

        return SafeOpt.empty();

    }

    @Override
    public <T> SafeOpt<IterIterableResult<T>> findBackwards(T[] array, IterIterableCons<T> iter) {
        Objects.requireNonNull(array, "Array is null");
        Objects.requireNonNull(iter, "Iteration logic is null");
        int[] bound = workoutBounds(array.length);
        int from = bound[0];
        int to = bound[1];
        IterIterableAccessor accessor = resolveAccessor(iter);
        for (int i = to - 1; i >= from; i--) {
            SafeOpt<IterIterableResult<T>> tryVisit = accessor.tryVisit(i, array[i], iter);
            if (tryVisit.hasValueOrError()) {
                return tryVisit;
            }
        }

        return SafeOpt.empty();
    }

    @Override
    public <T> SafeOpt<IterIterableResult<T>> find(T[] array, IterIterableCons<T> iter) {
        Objects.requireNonNull(array, "Array is null");
        Objects.requireNonNull(iter, "Iteration logic is null");
        int[] bound = workoutBounds(array.length);
        int from = bound[0];
        int to = bound[1];
        IterIterableAccessor accessor = resolveAccessor(iter);
        for (int i = from; i < to; i++) {
            SafeOpt<IterIterableResult<T>> tryVisit = accessor.tryVisit(i, array[i], iter);
            if (tryVisit.hasValueOrError()) {
                return tryVisit;
            }
        }

        return SafeOpt.empty();

    }

    @Override
    protected SimpleIterationIterable me() {
        return this;
    }

    @Override
    public <T> SafeOpt<IterIterableResult<T>> find(List<T> list, IterIterableCons<T> iter) {
        Objects.requireNonNull(list, "List is null");
        Objects.requireNonNull(iter, "Iteration logic is null");
        int[] bound = workoutBounds(list.size());
        int from = bound[0];
        int to = bound[1];
        ListIterator<T> iterator = list.listIterator(from);
        IterIterableAccessor accessor = resolveAccessor(iter);
        while (iterator.hasNext() && from < to) {
            T next = iterator.next();

            SafeOpt<IterIterableResult<T>> tryVisit = accessor.tryVisit(from, next, iter);
            if (tryVisit.hasValueOrError()) {
                return tryVisit;
            }
            from++;
        }

        return SafeOpt.empty();
    }

    @Override
    public <T> SafeOpt<IterIterableResult<T>> findBackwards(Deque<T> deque, IterIterableCons<T> iter) {
        Objects.requireNonNull(deque, "Deque is null");
        Objects.requireNonNull(iter, "Iteration logic is null");
        int size = deque.size();
        Iterator<T> descendingIterator = deque.descendingIterator();
        int[] bound = workoutBounds(size);
        int from = bound[0];
        int to = bound[1];
        int index = size;
        IterIterableAccessor accessor = resolveAccessor(iter);
        while (descendingIterator.hasNext()) {
            index--;
            T next = descendingIterator.next();
            if (index >= to) {
                continue;
            }

            if (index < from) {
                break;
            }

            SafeOpt<IterIterableResult<T>> tryVisit = accessor.tryVisit(index, next, iter);
            if (tryVisit.hasValueOrError()) {
                return tryVisit;
            }
        }

        return SafeOpt.empty();
    }

    @Override
    public <T> SafeOpt<IterIterableResult<T>> find(Iterator<T> iterator, IterIterableCons<T> iter) {
        Objects.requireNonNull(iterator, "Iterator is null");
        Objects.requireNonNull(iter, "Iteration logic is null");
        IterIterableAccessor accessor = resolveAccessor(iter);
        IterIterator<T> bufferedFind = bufferedFind(iterator);
        while (bufferedFind.hasNext()) {
            T next = bufferedFind.next();
            SafeOpt<IterIterableResult<T>> tryVisit = accessor.tryVisit(bufferedFind.getIndex(), next, iter);
            if (tryVisit.hasValueOrError()) {
                return tryVisit;
            }
        }
        return SafeOpt.empty();
    }

}
