package lt.lb.commons.iteration.general.impl;

import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
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
    public <T> Optional<IterIterableResult<T>> find(Iterator<T> iterator, IterIterableCons<T> iter) {
        return ImmutableImpl.find(iterator, resolveAccessor(iter), iter).asOptional();
    }

    @Override
    public <T> Optional<IterIterableResult<T>> find(List<T> list, IterIterableCons<T> iter) {
        return ImmutableImpl.find(list, resolveAccessor(iter), iter).asOptional();
    }

    @Override
    public <T> Optional<IterIterableResult<T>> find(T[] array, IterIterableCons<T> iter) {
        return ImmutableImpl.find(array, resolveAccessor(iter), iter).asOptional();
    }

    @Override
    public <T> Optional<IterIterableResult<T>> findBackwards(List<T> list, IterIterableCons<T> iter) {
        return ImmutableImpl.findBackwards(list, resolveAccessor(iter), iter).asOptional();
    }

    @Override
    public <T> Optional<IterIterableResult<T>> findBackwards(T[] array, IterIterableCons<T> iter) {
        return ImmutableImpl.findBackwards(array, resolveAccessor(iter), iter).asOptional();
    }

    @Override
    public <T> Optional<IterIterableResult<T>> findBackwards(Deque<T> deque, IterIterableCons<T> iter) {
        return ImmutableImpl.findBackwards(deque, resolveAccessor(iter), iter).asOptional();
    }

}
