package lt.lb.commons.iteration.general.impl;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Supplier;
import lt.lb.commons.SafeOpt;
import lt.lb.commons.containers.values.IntegerValue;
import lt.lb.commons.iteration.EmptyImmutableList;
import lt.lb.commons.iteration.ReadOnlyBidirectionalIterator;
import lt.lb.commons.iteration.ReadOnlyIterator;
import lt.lb.commons.iteration.general.IterationAbstract;
import lt.lb.commons.iteration.general.accessors.AccessorResolver;
import lt.lb.commons.iteration.general.accessors.DefaultAccessorResolver;
import lt.lb.commons.iteration.general.accessors.IterIterableAccessor;
import lt.lb.commons.iteration.general.accessors.IterMapAccessor;
import lt.lb.commons.iteration.general.cons.IterIterableCons;
import lt.lb.commons.iteration.general.cons.IterMapCons;
import lt.lb.commons.iteration.general.result.IterIterableResult;

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
        this.startingFrom = from;
        return me();
    }

    @Override
    public E endingBefore(int to) {
        this.endingBefore = to;
        return me();
    }

    @Override
    public E first(int amountToInclude) {
        this.onlyIncludingFirst = amountToInclude;
        this.onlyIncludingLast = -1;
        return me();
    }

    @Override
    public E last(int amountToInclude) {
        this.onlyIncludingLast = amountToInclude;
        this.onlyIncludingFirst = -1;
        return me();
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

    public static class IterIterator<T> implements Iterator<T> {

        private Iterator<T> iterator;
        private Supplier<Integer> index;

        public IterIterator(Iterator<T> iterator, Supplier<Integer> startingIndex) {
            this.iterator = iterator;
            this.index = startingIndex;
        }

        public int getIndex() {
            return index.get();
        }

        @Override
        public boolean hasNext() {
            return iterator.hasNext();
        }

        @Override
        public T next() {

            return iterator.next();
        }

    }

    protected <T> IterIterator<T> bufferedFind(Iterator<T> iterator) {
        Objects.requireNonNull(iterator, "Iteartor is null");

        SafeOpt<int[]> bounds = workoutBounds();
        if (bounds.isEmpty()) {
            return new IterIterator<>(EmptyImmutableList.emptyIterator(), () -> -1);
        }
        int[] b = bounds.get();
        final int from = b[0];
        final int to = b[1];

        int lastSize = 0;
        int index = -1;
        if (onlyIncludingLast > 0) {
            LinkedList<T> lastBuffer = new LinkedList<>();
            while (iterator.hasNext()) {
                index++;
                T next = iterator.next();
                if (index < from) {
                    continue;
                }
                if (to >= 0 && index >= to) {
                    break;
                }
                if (lastSize >= onlyIncludingLast) {
                    lastBuffer.addLast(next);
                    lastBuffer.removeFirst();
                } else {
                    lastBuffer.addLast(next);
                    lastSize++;
                }

            }
            ReadOnlyBidirectionalIterator<T> of = ReadOnlyIterator.of(lastBuffer);
            final int lastIndex = index - lastSize;
            // just iterate through the last elements
            return new IterIterator<>(of, () -> of.getCurrentIndex() + lastIndex);

        }

        final IntegerValue finalIndex = new IntegerValue(index);
        Iterator<T> iterator1 = new Iterator<T>() {

            T saved = null;

            int firstToInclude = onlyIncludingFirst;

            boolean hasNext = true;
            boolean nextAvailable = false;

            private boolean resolveNext() {
                if (hasNext && nextAvailable) {
                    return true;
                }

                while (iterator.hasNext()) {
                    int myIndex = finalIndex.incrementAndGet();
                    T next = iterator.next();
                    if (myIndex < from) {
                        continue;
                    }
                    if (to >= 0 && myIndex >= to) {

                        return false;
                    }
                    if (onlyIncludingFirst > 0) {
                        if (firstToInclude > 0) {
                            firstToInclude--;
                        } else {
                            return false;
                        }
                    }

                    saved = next;
                    nextAvailable = true;
                    return true;
                }
                return false;
            }

            @Override
            public boolean hasNext() {
                hasNext = resolveNext();
                return (hasNext && nextAvailable);
            }

            @Override
            public T next() {
                if (nextAvailable || hasNext()) {
                    nextAvailable = false;
                    return saved;
                }
                throw new NoSuchElementException("No next value");

            }
        };

        return new IterIterator<>(iterator1, () -> finalIndex.get());
    }

    protected AccessorResolver accessorResolver = new DefaultAccessorResolver();

    protected AccessorResolver getResolver() {
        return accessorResolver;
    }

    protected IterIterableAccessor resolveAccessor(IterIterableCons iter) {
        return getResolver().resolveAccessor(iter);
    }

    protected IterMapAccessor resolveAccessor(IterMapCons iter) {
        return getResolver().resolveAccessor(iter);
    }
}
