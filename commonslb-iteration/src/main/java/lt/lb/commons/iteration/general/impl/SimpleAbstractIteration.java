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
        int to;
        if (endingBefore < 0) {
            to = -1;
        } else {
            to = endingBefore;
        }

        int from;
        if (startingFrom < 0) {
            from = -1;
        } else {
            from = startingFrom;
        }

        if (onlyIncludingFirst == 0 || onlyIncludingLast == 0) {
            return SafeOpt.empty();
        }
        if (onlyIncludingFirst > 0 && onlyIncludingLast > 0) {
            throw new IllegalArgumentException("Can't include only first AND only last, please pick one or the other");
        }
        return SafeOpt.of(new int[]{from, to});
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
        int to;
        if (endingBefore < 0 || endingBefore > length) {
            to = length; // invalid, use default
        } else {
            to = endingBefore;
        }

        int from;
        if (startingFrom < 0 || startingFrom > length) {
            from = 0; // invalid, use default
        } else {
            from = startingFrom;
        }

        if (onlyIncludingFirst > 0 && onlyIncludingLast > 0) {
            throw new IllegalArgumentException("Can't include only first AND only last, please pick one or the other");
        }

        if (onlyIncludingFirst >= 0) {
            int size = to - from;
            if (size >= onlyIncludingFirst) {
                to = from + onlyIncludingFirst;
            }
        }

        if (onlyIncludingLast >= 0) {
            int size = to - from;
            if (size >= onlyIncludingLast) {
                from = to - onlyIncludingLast;
            }
        }

        return new int[]{from, to};
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
