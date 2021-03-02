package lt.lb.commons.iteration.general.impl;

import java.util.Collection;
import java.util.Objects;
import lt.lb.commons.SafeOpt;
import lt.lb.commons.iteration.general.IterationAbstract;
import lt.lb.commons.iteration.general.accessors.AccessorResolver;
import lt.lb.commons.iteration.general.accessors.DefaultAccessorResolver;
import lt.lb.commons.iteration.general.accessors.IterIterableAccessor;
import lt.lb.commons.iteration.general.accessors.IterMapAccessor;
import lt.lb.commons.iteration.general.cons.IterIterableCons;
import lt.lb.commons.iteration.general.cons.IterMapCons;

/**
 *
 * @author laim0nas100
 */
public abstract class SimpleAbstractIteration<E extends SimpleAbstractIteration<E>> implements IterationAbstract<E> {

    protected int onlyIncludingLast = -1;
    protected int onlyIncludingFirst = -1;

    protected int startingFrom = -1;
    protected int endingBefore = -1;

    @Override
    public E startingFrom(int from) {
        E me = me();
        me.startingFrom = from;
        return me;
    }

    @Override
    public E endingBefore(int to) {
        E me = me();
        me.endingBefore = to;
        return me;
    }

    @Override
    public E first(int amountToInclude) {
        E me = me();
        me.onlyIncludingFirst = amountToInclude;
        me.onlyIncludingLast = -1;
        return me;
    }

    @Override
    public E last(int amountToInclude) {
        E me = me();
        me.onlyIncludingLast = amountToInclude;
        me.onlyIncludingFirst = -1;
        return me;
    }

    protected abstract E me();

    protected SafeOpt<int[]> workoutBounds() {
        return SimpleImpl.workoutBounds(endingBefore, startingFrom, onlyIncludingFirst, onlyIncludingLast);
    }

    protected int[] workoutBounds(Collection col) {
        Objects.requireNonNull(col, "Collection is null");
        return workoutBounds(col.size());
    }

    protected <T> int[] workoutBounds(T[] array) {
        Objects.requireNonNull(array, "Collection is null");
        return workoutBounds(array.length);
    }

    protected int[] workoutBounds(int length) {
        return SimpleImpl.workoutBounds(length, endingBefore, startingFrom, onlyIncludingFirst, onlyIncludingLast);
    }

    protected AccessorResolver accessorResolver = new DefaultAccessorResolver();

    protected AccessorResolver getResolver() {
        return accessorResolver;
    }

    protected IterIterableAccessor resolveAccessor(IterIterableCons iter) {
        Objects.requireNonNull(iter, "Iteration logic is null");
        return getResolver().resolveAccessor(iter);
    }

    protected IterMapAccessor resolveAccessor(IterMapCons iter) {
        Objects.requireNonNull(iter, "Iteration logic is null");
        return getResolver().resolveAccessor(iter);
    }
}
