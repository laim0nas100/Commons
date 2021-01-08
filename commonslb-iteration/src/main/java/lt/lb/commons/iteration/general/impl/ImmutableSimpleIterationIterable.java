package lt.lb.commons.iteration.general.impl;

import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import lt.lb.commons.SafeOpt;
import lt.lb.commons.iteration.general.cons.IterIterableCons;
import lt.lb.commons.iteration.general.result.IterIterableResult;

/**
 *
 * Pure iteration, with no parameters, optimized.
 *
 * @author laim0nas100
 */
public class ImmutableSimpleIterationIterable extends SimpleIterationIterable {

    @Override
    protected SimpleIterationIterable me() {
        return new SimpleIterationIterable();
    }

    @Override
    public SimpleIterationIterable endingBefore(int to) {
        return new SimpleIterationIterable().endingBefore(to);
    }

    @Override
    public SimpleIterationIterable startingFrom(int from) {
        return new SimpleIterationIterable().startingFrom(from);
    }

    @Override
    public SimpleIterationIterable last(int amountToInclude) {
        return new SimpleIterationIterable().last(amountToInclude);
    }

    @Override
    public SimpleIterationIterable first(int amountToInclude) {
        return new SimpleIterationIterable().first(amountToInclude);
    }

    @Override
    public SimpleIterationIterable withInterval(int from, int to) {
        return new SimpleIterationIterable().withInterval(from, to);
    }

    @Override
    public <T> SafeOpt<IterIterableResult<T>> find(Iterator<T> iterator, IterIterableCons<T> iter) {
        IterIterableAccessor accessor = resolveAccessor(iter);
        int index = 0;
        while (iterator.hasNext()) {
            T next = iterator.next();
            SafeOpt<IterIterableResult<T>> tryVisit = accessor.tryVisit(index, next, iter);
            if (tryVisit.hasValueOrError()) {
                return tryVisit;
            }
            index++;
        }
        return SafeOpt.empty();
    }

    @Override
    public <T> SafeOpt<IterIterableResult<T>> find(List<T> list, IterIterableCons<T> iter) {
        ListIterator<T> iterator = list.listIterator(0);
        int index = 0;
        IterIterableAccessor accessor = resolveAccessor(iter);
        while (iterator.hasNext()) {
            T next = iterator.next();
            SafeOpt<IterIterableResult<T>> tryVisit = accessor.tryVisit(index, next, iter);
            if (tryVisit.hasValueOrError()) {
                return tryVisit;
            }
            index++;

        }
        return SafeOpt.empty();
    }

    @Override
    public <T> SafeOpt<IterIterableResult<T>> find(T[] array, IterIterableCons<T> iter) {
        IterIterableAccessor accessor = resolveAccessor(iter);
        for (int i = 0; i < array.length; i++) {
            SafeOpt<IterIterableResult<T>> tryVisit = accessor.tryVisit(i, array[i], iter);
            if (tryVisit.hasValueOrError()) {
                return tryVisit;
            }
        }
        return SafeOpt.empty();
    }

    @Override
    public <T> SafeOpt<IterIterableResult<T>> findBackwards(List<T> list, IterIterableCons<T> iter) {
        int size = list.size();
        ListIterator<T> iterator = list.listIterator(size);
        int index = size;
        IterIterableAccessor accessor = resolveAccessor(iter);
        while (iterator.hasPrevious()) {
            index--;
            T next = iterator.previous();
            SafeOpt<IterIterableResult<T>> tryVisit = accessor.tryVisit(index, next, iter);
            if (tryVisit.hasValueOrError()) {
                return tryVisit;
            }
        }
        return SafeOpt.empty();
    }

    @Override
    public <T> SafeOpt<IterIterableResult<T>> findBackwards(T[] array, IterIterableCons<T> iter) {
        IterIterableAccessor accessor = resolveAccessor(iter);
        for (int i = array.length - 1; i >= 0; i--) {
            SafeOpt<IterIterableResult<T>> tryVisit = accessor.tryVisit(i, array[i], iter);
            if (tryVisit.hasValueOrError()) {
                return tryVisit;
            }
        }
        return SafeOpt.empty();
    }

    @Override
    public <T> SafeOpt<IterIterableResult<T>> findBackwards(Deque<T> deque, IterIterableCons<T> iter) {
        int size = deque.size();
        Iterator<T> descendingIterator = deque.descendingIterator();
        int index = size - 1;
        IterIterableAccessor accessor = resolveAccessor(iter);
        while (descendingIterator.hasNext()) {
            T next = descendingIterator.next();

            SafeOpt<IterIterableResult<T>> tryVisit = accessor.tryVisit(index, next, iter);
            if (tryVisit.hasValueOrError()) {
                return tryVisit;
            }

            index--;

        }

        return SafeOpt.empty();
    }

}
