package lt.lb.commons.iteration.general.impl.unchecked;

import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import lt.lb.uncheckedutils.SafeOpt;
import lt.lb.commons.iteration.general.cons.unchecked.IterIterableConsUnchecked;
import lt.lb.commons.iteration.general.impl.*;
import lt.lb.commons.iteration.general.result.IterIterableResult;

/**
 *
 * Pure iteration, with no parameters, optimized.
 *
 * @author laim0nas100
 */
public class ImmutableSimpleIterationIterableUnchecked extends SimpleIterationIterableUnchecked {

    @Override
    protected SimpleIterationIterableUnchecked me() {
        return new SimpleIterationIterableUnchecked();
    }

    @Override
    public <T> SafeOpt<IterIterableResult<T>> find(Iterator<T> iterator, IterIterableConsUnchecked<T> iter) {
        return ImmutableImpl.find(iterator, resolveAccessor(iter), iter);
    }

    @Override
    public <T> SafeOpt<IterIterableResult<T>> find(List<T> list, IterIterableConsUnchecked<T> iter) {
        return ImmutableImpl.find(list, resolveAccessor(iter), iter);
    }

    @Override
    public <T> SafeOpt<IterIterableResult<T>> find(T[] array, IterIterableConsUnchecked<T> iter) {
        return ImmutableImpl.find(array, resolveAccessor(iter), iter);
    }

    @Override
    public <T> SafeOpt<IterIterableResult<T>> findBackwards(List<T> list, IterIterableConsUnchecked<T> iter) {
        return ImmutableImpl.findBackwards(list, resolveAccessor(iter), iter);
    }

    @Override
    public <T> SafeOpt<IterIterableResult<T>> findBackwards(Deque<T> deque, IterIterableConsUnchecked<T> iter) {
        return ImmutableImpl.findBackwards(deque, resolveAccessor(iter), iter);
    }

    @Override
    public <T> SafeOpt<IterIterableResult<T>> findBackwards(T[] array, IterIterableConsUnchecked<T> iter) {
        return ImmutableImpl.findBackwards(array, resolveAccessor(iter), iter);
    }

}
