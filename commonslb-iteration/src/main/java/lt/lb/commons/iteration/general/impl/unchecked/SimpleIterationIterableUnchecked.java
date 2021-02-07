package lt.lb.commons.iteration.general.impl.unchecked;

import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import lt.lb.commons.SafeOpt;
import lt.lb.commons.iteration.general.IterationIterableUnchecked;
import lt.lb.commons.iteration.general.accessors.AccessorResolver;
import lt.lb.commons.iteration.general.accessors.unchecked.DefaultAccessorResolverUnchecked;
import lt.lb.commons.iteration.general.cons.unchecked.IterIterableConsUnchecked;
import lt.lb.commons.iteration.general.impl.*;
import lt.lb.commons.iteration.general.result.IterIterableResult;

/**
 *
 * @author laim0nas100
 */
public class SimpleIterationIterableUnchecked extends SimpleAbstractIteration<SimpleIterationIterableUnchecked> implements IterationIterableUnchecked<SimpleIterationIterableUnchecked> {

    protected AccessorResolver resolver = new DefaultAccessorResolverUnchecked();
    protected SimpleIterationIterable main = new SimpleIterationIterable(){
        @Override
        protected AccessorResolver getResolver() {
            return resolver;
        }
    
        
    };

    @Override
    public SimpleIterationIterableUnchecked startingFrom(int from) {
        main.startingFrom(from);
        return this;
    }

    @Override
    public SimpleIterationIterableUnchecked endingBefore(int to) {
        main.endingBefore(to);
        return this;
    }

    @Override
    protected SimpleIterationIterableUnchecked me() {
        return this;
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
