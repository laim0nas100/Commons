package com.github.laim0nas100.commonslb.iteration.general.impl.unchecked;

import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import com.github.laim0nas100.commonslb.iteration.general.IterationIterableUnchecked;
import com.github.laim0nas100.commonslb.iteration.general.accessors.unchecked.DefaultAccessorResolverUnchecked;
import com.github.laim0nas100.commonslb.iteration.general.cons.unchecked.IterIterableConsUnchecked;
import com.github.laim0nas100.commonslb.iteration.general.impl.*;
import com.github.laim0nas100.commonslb.iteration.general.result.IterIterableResult;
import com.github.laim0nas100.uncheckedutils.SafeOpt;

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
