package lt.lb.commons.iteration.general.impl.unchecked;

import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import lt.lb.commons.SafeOpt;
import lt.lb.commons.iteration.general.accessors.AccessorResolver;
import lt.lb.commons.iteration.general.accessors.unchecked.DefaultAccessorResolverUnchecked;
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

    protected AccessorResolver resolver = new DefaultAccessorResolverUnchecked();
    protected ImmutableSimpleIterationIterable main = new ImmutableSimpleIterationIterable() {
        @Override
        protected AccessorResolver getResolver() {
            return resolver;
        }

    };

    @Override
    protected SimpleIterationIterableUnchecked me() {
        return new SimpleIterationIterableUnchecked();
    }

    @Override
    public SimpleIterationIterableUnchecked endingBefore(int to) {
        return me().endingBefore(to);
    }

    @Override
    public SimpleIterationIterableUnchecked startingFrom(int from) {
        return me().startingFrom(from);
    }

    @Override
    public SimpleIterationIterableUnchecked last(int amountToInclude) {
        return me().last(amountToInclude);
    }

    @Override
    public SimpleIterationIterableUnchecked first(int amountToInclude) {
        return me().first(amountToInclude);
    }

    @Override
    public SimpleIterationIterableUnchecked withInterval(int from, int to) {
        return me().withInterval(from, to);
    }

    
    
    @Override
    public <T> SafeOpt<IterIterableResult<T>> find(Iterator<T> iterator, IterIterableConsUnchecked<T> iter) {
        return main.find(iterator, iter);
    }

    @Override
    public <T> SafeOpt<IterIterableResult<T>> find(List<T> list, IterIterableConsUnchecked<T> iter) {
        return main.find(list, iter);
    }

    @Override
    public <T> SafeOpt<IterIterableResult<T>> find(T[] array, IterIterableConsUnchecked<T> iter) {
        return main.find(array, iter);
    }

    @Override
    public <T> SafeOpt<IterIterableResult<T>> findBackwards(List<T> list, IterIterableConsUnchecked<T> iter) {
        return main.findBackwards(list, iter);
    }

    @Override
    public <T> SafeOpt<IterIterableResult<T>> findBackwards(Deque<T> deque, IterIterableConsUnchecked<T> iter) {
        return main.findBackwards(deque, iter);
    }

    @Override
    public <T> SafeOpt<IterIterableResult<T>> findBackwards(T[] array, IterIterableConsUnchecked<T> iter) {
        return main.findBackwards(array, iter);
    }

}
