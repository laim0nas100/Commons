package lt.lb.commons.iteration.general.impl;

import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Optional;
import lt.lb.commons.iteration.Iter;
import lt.lb.commons.iteration.ReadOnlyIterator;
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
    public <T> Optional<IterIterableResult<T>> find(ReadOnlyIterator<T> iterator, Iter<T> iter) {
        int index = 0;
        while (iterator.hasNext()) {
            index++;
            T next = iterator.next();
            if (iter.visit(index, next)) {
                return Optional.of(new IterIterableResult<>(index, next));
            }
        }
        return Optional.empty();
    }

    @Override
    public <T> Optional<IterIterableResult<T>> find(List<T> list, Iter<T> iter) {
        ListIterator<T> iterator = list.listIterator(0);
        int index = 0;
        while (iterator.hasNext()) {
            T next = iterator.next();
            if (iter.visit(index, next)) {
                return Optional.of(new IterIterableResult<>(index, next));
            }
            index++;

        }
        return Optional.empty();
    }

    @Override
    public <T> Optional<IterIterableResult<T>> find(T[] array, Iter<T> iter) {
        for (int i = 0; i < array.length; i++) {
            if (iter.visit(i, array[i])) {
                return Optional.of(new IterIterableResult<>(i, array[i]));
            }
        }
        return Optional.empty();
    }

    @Override
    public <T> Optional<IterIterableResult<T>> findBackwards(List<T> list, Iter<T> iter) {
        int size = list.size();
        ListIterator<T> iterator = list.listIterator(size);
        int index = size;
        while (iterator.hasPrevious()) {
            index--;
            T next = iterator.previous();
            if (iter.visit(index, next)) {
                return Optional.of(new IterIterableResult<>(index, next));
            }
        }
        return Optional.empty();
    }

    @Override
    public <T> Optional<IterIterableResult<T>> findBackwards(T[] array, Iter<T> iter) {
        for (int i = array.length - 1; i >= 0; i--) {
            if (iter.visit(i, array[i])) {
                return Optional.of(new IterIterableResult<>(i, array[i]));
            }
        }
        return Optional.empty();
    }
    
    @Override
    public <T> Optional<IterIterableResult<T>> findBackwards(Deque<T> deque, Iter<T> iter) {
        int size = deque.size();
        Iterator<T> descendingIterator = deque.descendingIterator();
        int index = size - 1;
        while (descendingIterator.hasNext()) {
            T next = descendingIterator.next();
            
            if (iter.visit(index, next)) {
                return Optional.of(new IterIterableResult<>(index, next));
            }
            
            index--;
            
        }

        return Optional.empty();
    }

}
