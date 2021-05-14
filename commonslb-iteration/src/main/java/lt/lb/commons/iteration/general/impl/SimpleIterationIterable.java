package lt.lb.commons.iteration.general.impl;

import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import lt.lb.commons.iteration.general.IterationIterable;
import lt.lb.commons.iteration.general.cons.IterIterableCons;
import lt.lb.commons.iteration.general.result.IterIterableResult;

/**
 *
 * @author laim0nas100
 */
public class SimpleIterationIterable extends SimpleAbstractIteration<SimpleIterationIterable> implements IterationIterable<SimpleIterationIterable> {

    @Override
    public <T> Optional<IterIterableResult<T>> findBackwards(List<T> list, IterIterableCons<T> iter) {
        return SimpleImpl.findBackwards(list, workoutBounds(list), resolveAccessor(iter), iter).asOptional();
    }

    @Override
    public <T> Optional<IterIterableResult<T>> findBackwards(T[] array, IterIterableCons<T> iter) {
        return SimpleImpl.findBackwards(array, workoutBounds(array), resolveAccessor(iter), iter).asOptional();
    }

    @Override
    public <T> Optional<IterIterableResult<T>> find(T[] array, IterIterableCons<T> iter) {
        return SimpleImpl.find(array, workoutBounds(array), resolveAccessor(iter), iter).asOptional();
    }

    @Override
    protected SimpleIterationIterable me() {
        return this;
    }

    @Override
    public <T> Optional<IterIterableResult<T>> find(List<T> list, IterIterableCons<T> iter) {
        return SimpleImpl.find(list, workoutBounds(list), resolveAccessor(iter), iter).asOptional();
    }

    @Override
    public <T> Optional<IterIterableResult<T>> findBackwards(Deque<T> deque, IterIterableCons<T> iter) {
        return SimpleImpl.findBackwards(deque, workoutBounds(deque), resolveAccessor(iter), iter).asOptional();
    }

    @Override
    public <T> Optional<IterIterableResult<T>> find(Iterator<T> iterator, IterIterableCons<T> iter) {
        return SimpleImpl.find(iterator, workoutBounds(), onlyIncludingFirst, onlyIncludingLast, resolveAccessor(iter), iter).asOptional();
    }

}
