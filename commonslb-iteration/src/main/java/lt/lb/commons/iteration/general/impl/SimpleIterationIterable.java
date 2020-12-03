package lt.lb.commons.iteration.general.impl;

import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Optional;
import lt.lb.commons.iteration.For;
import lt.lb.commons.iteration.ReadOnlyIterator;
import lt.lb.commons.iteration.general.IterationIterable;
import lt.lb.commons.iteration.general.cons.IterIterableCons;
import lt.lb.commons.iteration.general.result.IterIterableResult;

/**
 *
 * @author laim0nas100
 */
public class SimpleIterationIterable extends SimpleAbstractIteration<SimpleIterationIterable> implements IterationIterable<SimpleIterationIterable> {

    protected int startingFrom = -1;
    protected int endingBefore = -1;

    @Override
    public SimpleIterationIterable startingFrom(int from) {
        this.startingFrom = from;
        return this;
    }

    @Override
    public SimpleIterationIterable endingBefore(int to) {
        this.endingBefore = to;
        return this;
    }

    @Override
    public <T> Optional<IterIterableResult<T>> find(ReadOnlyIterator<T> iterator, IterIterableCons<T> iter) {
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
            return Optional.empty();
        }
        if (onlyIncludingFirst > 0 && onlyIncludingLast > 0) {
            throw new IllegalArgumentException("Can't include only first AND only last, please pick one or the other");
        }

        int lastSize = 0;
        int index = -1;
        LinkedList<T> lastBuffer = null;
        IterIterableAccessor accessor = resolveAccessor(iter);
        if (onlyIncludingLast > 0) {
            lastBuffer = new LinkedList<>();
            boolean reachedEnd = false;
            while (iterator.hasNext() && !reachedEnd) {
                index++;
                T next = iterator.next();
                if (from >= 0 && index < from) {
                    continue;
                }
                if (to >= 0 && index >= to) {
                    reachedEnd = true;
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

            int lastIndex = index - lastSize;
            // just iterate through the last elements
            for (T res : lastBuffer) {
                Optional<IterIterableResult<T>> tryVisit = accessor.tryVisit(lastIndex, res, iter);
                if (tryVisit.isPresent()) {
                    return tryVisit;
                }
                lastIndex++;
            }

        } else {
            int firstToInclude = onlyIncludingFirst;
            while (iterator.hasNext()) {
                index++;
                T next = iterator.next();
                if (to >= 0 && index >= to) {
                    return Optional.empty();
                }
                if (from >= 0 && index < from) {
                    continue;
                }
                if (onlyIncludingFirst > 0) {
                    if (firstToInclude > 0) {
                        firstToInclude--;
                    } else {
                        return Optional.empty();
                    }
                }

                Optional<IterIterableResult<T>> tryVisit = accessor.tryVisit(index, next, iter);
                if (tryVisit.isPresent()) {
                    return tryVisit;
                }

            }
        }

        return Optional.empty();

    }

    @Override
    public <T> Optional<IterIterableResult<T>> findBackwards(List<T> list, IterIterableCons<T> iter) {
        int[] bound = workoutBounds(list.size());
        int from = bound[0];
        int to = bound[1];
        ListIterator<T> iterator = list.listIterator(to);
        to--;
        IterIterableAccessor accessor = resolveAccessor(iter);
        while (iterator.hasPrevious() && from <= to) {
            T next = iterator.previous();

            Optional<IterIterableResult<T>> tryVisit = accessor.tryVisit(to, next, iter);
            if (tryVisit.isPresent()) {
                return tryVisit;
            }
            to--;
        }

        return Optional.empty();

    }

    protected int[] workoutBounds(int length) {
        int to;
        if (endingBefore < 0 || endingBefore > length) {
            to = length;
        } else {
            to = endingBefore;
        }

        int from;
        if (startingFrom < 0 || startingFrom > length) {
            from = 0;
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

    @Override
    public <T> Optional<IterIterableResult<T>> findBackwards(T[] array, IterIterableCons<T> iter) {
        int[] bound = workoutBounds(array.length);
        int from = bound[0];
        int to = bound[1];
        IterIterableAccessor accessor = resolveAccessor(iter);
        for (int i = to - 1; i >= from; i--) {
            Optional<IterIterableResult<T>> tryVisit = accessor.tryVisit(i, array[i], iter);
            if (tryVisit.isPresent()) {
                return tryVisit;
            }
        }

        return Optional.empty();
    }

    @Override
    public <T> Optional<IterIterableResult<T>> find(T[] array, IterIterableCons<T> iter) {
        int[] bound = workoutBounds(array.length);
        int from = bound[0];
        int to = bound[1];
        IterIterableAccessor accessor = resolveAccessor(iter);
        for (int i = from; i < to; i++) {
            Optional<IterIterableResult<T>> tryVisit = accessor.tryVisit(i, array[i], iter);
            if (tryVisit.isPresent()) {
                return tryVisit;
            }
        }

        return Optional.empty();

    }

    @Override
    protected SimpleIterationIterable me() {
        return this;
    }

    @Override
    public <T> Optional<IterIterableResult<T>> find(List<T> list, IterIterableCons<T> iter) {
        int[] bound = workoutBounds(list.size());
        int from = bound[0];
        int to = bound[1];
        ListIterator<T> iterator = list.listIterator(from);
        IterIterableAccessor accessor = resolveAccessor(iter);
        while (iterator.hasNext() && from < to) {
            T next = iterator.next();

            Optional<IterIterableResult<T>> tryVisit = accessor.tryVisit(from, next, iter);
            if (tryVisit.isPresent()) {
                return tryVisit;
            }
            from++;
        }

        return Optional.empty();
    }

    @Override
    public <T> Optional<IterIterableResult<T>> findBackwards(Deque<T> deque, IterIterableCons<T> iter) {
        int size = deque.size();
        Iterator<T> descendingIterator = deque.descendingIterator();
        int[] bound = workoutBounds(size);
        int from = bound[0];
        int to = bound[1];
        int index = size;
        IterIterableAccessor accessor = resolveAccessor(iter);
        while (descendingIterator.hasNext()) {
            index--;
            T next = descendingIterator.next();
            if (index >= to) {
                continue;
            }

            if (index < from) {
                break;
            }

            Optional<IterIterableResult<T>> tryVisit = accessor.tryVisit(index, next, iter);
            if (tryVisit.isPresent()) {
                return tryVisit;
            }
        }

        return Optional.empty();
    }

}
