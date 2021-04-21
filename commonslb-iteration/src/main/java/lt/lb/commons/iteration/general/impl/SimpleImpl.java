package lt.lb.commons.iteration.general.impl;

import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Supplier;
import lt.lb.uncheckedutils.SafeOpt;
import lt.lb.commons.containers.values.IntegerValue;
import lt.lb.commons.iteration.EmptyImmutableList;
import lt.lb.commons.iteration.ReadOnlyBidirectionalIterator;
import lt.lb.commons.iteration.ReadOnlyIterator;
import lt.lb.commons.iteration.general.accessors.IterIterableAccessor;
import lt.lb.commons.iteration.general.accessors.IterMapAccessor;
import lt.lb.commons.iteration.general.cons.IterIterableCons;
import lt.lb.commons.iteration.general.cons.IterMapCons;
import lt.lb.commons.iteration.general.result.IterIterableResult;
import lt.lb.commons.iteration.general.result.IterMapResult;

/**
 *
 * Iteration implementation with restrictions. Not as fast as unrestricted
 * {@link ImmutableImpl}.
 *
 * @author laim0nas100
 */
public class SimpleImpl {

    public static <T> SafeOpt<IterIterableResult<T>> findBackwards(List<T> list, int[] bound, IterIterableAccessor accessor, IterIterableCons<T> iter) {
        Objects.requireNonNull(list, "List is null");
        Objects.requireNonNull(iter, "Iteration logic is null");
        Objects.requireNonNull(accessor, "Accessor is null");
        int from = bound[0];
        int to = bound[1];
        ListIterator<T> iterator = list.listIterator(to);
        to--;
        while (iterator.hasPrevious() && from <= to) {
            T next = iterator.previous();

            SafeOpt<IterIterableResult<T>> tryVisit = accessor.tryVisit(to, next, iter);
            if (tryVisit.hasValueOrError()) {
                return tryVisit;
            }
            to--;
        }

        return SafeOpt.empty();

    }

    public static <T> SafeOpt<IterIterableResult<T>> findBackwards(T[] array, int[] bound, IterIterableAccessor accessor, IterIterableCons<T> iter) {
        Objects.requireNonNull(array, "Array is null");
        Objects.requireNonNull(iter, "Iteration logic is null");
        Objects.requireNonNull(accessor, "Accessor is null");
        int from = bound[0];
        int to = bound[1];
        for (int i = to - 1; i >= from; i--) {
            SafeOpt<IterIterableResult<T>> tryVisit = accessor.tryVisit(i, array[i], iter);
            if (tryVisit.hasValueOrError()) {
                return tryVisit;
            }
        }

        return SafeOpt.empty();
    }

    public static <T> SafeOpt<IterIterableResult<T>> find(T[] array, int[] bound, IterIterableAccessor accessor, IterIterableCons<T> iter) {
        Objects.requireNonNull(array, "Array is null");
        Objects.requireNonNull(iter, "Iteration logic is null");
        Objects.requireNonNull(accessor, "Accessor is null");
        int from = bound[0];
        int to = bound[1];
        for (int i = from; i < to; i++) {
            SafeOpt<IterIterableResult<T>> tryVisit = accessor.tryVisit(i, array[i], iter);
            if (tryVisit.hasValueOrError()) {
                return tryVisit;
            }
        }

        return SafeOpt.empty();

    }

    public static <T> SafeOpt<IterIterableResult<T>> find(List<T> list, int[] bound, IterIterableAccessor accessor, IterIterableCons<T> iter) {
        Objects.requireNonNull(list, "List is null");
        Objects.requireNonNull(iter, "Iteration logic is null");
        Objects.requireNonNull(accessor, "Accessor is null");
        int from = bound[0];
        int to = bound[1];
        ListIterator<T> iterator = list.listIterator(from);
        while (iterator.hasNext() && from < to) {
            T next = iterator.next();

            SafeOpt<IterIterableResult<T>> tryVisit = accessor.tryVisit(from, next, iter);
            if (tryVisit.hasValueOrError()) {
                return tryVisit;
            }
            from++;
        }

        return SafeOpt.empty();
    }

    public static <T> SafeOpt<IterIterableResult<T>> findBackwards(Deque<T> deque, int[] bound, IterIterableAccessor accessor, IterIterableCons<T> iter) {
        Objects.requireNonNull(deque, "Deque is null");
        Objects.requireNonNull(iter, "Iteration logic is null");
        Objects.requireNonNull(accessor, "Accessor is null");
        int size = deque.size();
        Iterator<T> descendingIterator = deque.descendingIterator();
        int from = bound[0];
        int to = bound[1];
        int index = size;
        while (descendingIterator.hasNext()) {
            index--;
            T next = descendingIterator.next();
            if (index >= to) {
                continue;
            }

            if (index < from) {
                break;
            }

            SafeOpt<IterIterableResult<T>> tryVisit = accessor.tryVisit(index, next, iter);
            if (tryVisit.hasValueOrError()) {
                return tryVisit;
            }
        }

        return SafeOpt.empty();
    }

    public static <T> SafeOpt<IterIterableResult<T>> find(Iterator<T> iterator, SafeOpt<int[]> bounds, int onlyIncludingFirst, int onlyIncludingLast, IterIterableAccessor accessor, IterIterableCons<T> iter) {
        Objects.requireNonNull(iterator, "Iterator is null");
        Objects.requireNonNull(iter, "Iteration logic is null");
        Objects.requireNonNull(accessor, "Accessor is null");
        IterIterator<T> bufferedFind = bufferedFind(iterator, bounds, onlyIncludingFirst, onlyIncludingLast);
        while (bufferedFind.hasNext()) {
            T next = bufferedFind.next();
            SafeOpt<IterIterableResult<T>> tryVisit = accessor.tryVisit(bufferedFind.getIndex(), next, iter);
            if (tryVisit.hasValueOrError()) {
                return tryVisit;
            }
        }
        return SafeOpt.empty();
    }

    public static <K, V> SafeOpt<IterMapResult<K, V>> find(Map<K, V> map, SafeOpt<int[]> bounds, int onlyIncludingFirst, int onlyIncludingLast, IterMapAccessor accessor, IterMapCons<K, V> iter) {
        Objects.requireNonNull(map, "Map is null");
        Objects.requireNonNull(iter, "Iteration logic is null");
        Objects.requireNonNull(accessor, "Accessor is null");
        IterIterator<Map.Entry<K, V>> bufferedFind = bufferedFind(map.entrySet().iterator(), bounds, onlyIncludingFirst, onlyIncludingLast);
        while (bufferedFind.hasNext()) {
            Map.Entry<K, V> entry = bufferedFind.next();
            SafeOpt<IterMapResult<K, V>> tryVisit = accessor.tryVisit(bufferedFind.getIndex(), entry.getKey(), entry.getValue(), iter);
            if (tryVisit.hasValueOrError()) {
                return tryVisit;
            }
        }
        return SafeOpt.empty();

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

    public static <T> IterIterator<T> bufferedFind(Iterator<T> iterator, SafeOpt<int[]> bounds, int onlyIncludingFirst, int onlyIncludingLast) {
        Objects.requireNonNull(iterator, "Iteartor is null");

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
    
    
    public static int[] workoutBounds(int length, int endingBefore, int startingFrom, int onlyIncludingFirst, int onlyIncludingLast) {
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
    
    public static SafeOpt<int[]> workoutBounds(int endingBefore, int startingFrom, int onlyIncludingFirst, int onlyIncludingLast) {
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
}
