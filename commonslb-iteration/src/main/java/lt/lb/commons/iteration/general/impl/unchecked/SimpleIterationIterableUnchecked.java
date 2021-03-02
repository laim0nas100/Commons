package lt.lb.commons.iteration.general.impl.unchecked;

import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import lt.lb.commons.SafeOpt;
import lt.lb.commons.iteration.general.IterationIterableUnchecked;
import lt.lb.commons.iteration.general.accessors.unchecked.DefaultAccessorResolverUnchecked;
import lt.lb.commons.iteration.general.cons.unchecked.IterIterableConsUnchecked;
import lt.lb.commons.iteration.general.impl.*;
import lt.lb.commons.iteration.general.result.IterIterableResult;

/**
 *
 * @author laim0nas100
 */
public class SimpleIterationIterableUnchecked extends SimpleAbstractIteration<SimpleIterationIterableUnchecked> implements IterationIterableUnchecked<SimpleIterationIterableUnchecked> {

    public SimpleIterationIterableUnchecked() {
        this.accessorResolver = new DefaultAccessorResolverUnchecked();
    }

    @Override
    protected SimpleIterationIterableUnchecked me() {
        return this;
    }

    @Override
    public <T> SafeOpt<IterIterableResult<T>> find(Iterator<T> iterator, IterIterableConsUnchecked<T> iter) {
        return SimpleImpl.find(iterator, workoutBounds(), onlyIncludingFirst, onlyIncludingLast, resolveAccessor(iter), iter);
    }

    @Override
    public <T> SafeOpt<IterIterableResult<T>> find(List<T> list, IterIterableConsUnchecked<T> iter) {
        return SimpleImpl.find(list, workoutBounds(list), resolveAccessor(iter), iter);
    }

    @Override
    public <T> SafeOpt<IterIterableResult<T>> find(T[] array, IterIterableConsUnchecked<T> iter) {
        return SimpleImpl.find(array, workoutBounds(array), resolveAccessor(iter), iter);
    }

    @Override
    public <T> SafeOpt<IterIterableResult<T>> findBackwards(List<T> list, IterIterableConsUnchecked<T> iter) {
        return SimpleImpl.findBackwards(list, workoutBounds(list), resolveAccessor(iter), iter);
    }

    @Override
    public <T> SafeOpt<IterIterableResult<T>> findBackwards(Deque<T> deque, IterIterableConsUnchecked<T> iter) {
        return SimpleImpl.findBackwards(deque, workoutBounds(deque), resolveAccessor(iter), iter);
    }

    @Override
    public <T> SafeOpt<IterIterableResult<T>> findBackwards(T[] array, IterIterableConsUnchecked<T> iter) {
        return SimpleImpl.findBackwards(array, workoutBounds(array), resolveAccessor(iter), iter);
    }

}
