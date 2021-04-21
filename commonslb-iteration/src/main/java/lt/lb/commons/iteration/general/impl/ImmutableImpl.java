package lt.lb.commons.iteration.general.impl;

import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Objects;
import lt.lb.uncheckedutils.SafeOpt;
import lt.lb.commons.iteration.general.accessors.IterIterableAccessor;
import lt.lb.commons.iteration.general.accessors.IterMapAccessor;
import lt.lb.commons.iteration.general.cons.IterIterableCons;
import lt.lb.commons.iteration.general.cons.IterMapCons;
import lt.lb.commons.iteration.general.result.IterIterableResult;
import lt.lb.commons.iteration.general.result.IterMapResult;

/**
 * Iteration implementation without restrictions. Should be as fast as possible.
 * @author laim0nas100
 */
public class ImmutableImpl {

    public static <T> SafeOpt<IterIterableResult<T>> find(Iterator<T> iterator, IterIterableAccessor accessor, IterIterableCons<T> iter) {
        Objects.requireNonNull(iterator, "Iterator is null");
        Objects.requireNonNull(iter, "Iteration logic is null");
        Objects.requireNonNull(accessor, "Accessor is null");
        int index = 0;
        while (iterator.hasNext()) {
            T next = iterator.next();
            SafeOpt<IterIterableResult<T>> tryVisit = accessor.tryVisit(index, next, iter);
            if (tryVisit.hasValueOrError()) {
                return tryVisit;
            }
            index++;
        }
        return SafeOpt.empty();
    }

    public static <T> SafeOpt<IterIterableResult<T>> find(List<T> list, IterIterableAccessor accessor, IterIterableCons<T> iter) {
        Objects.requireNonNull(list, "List is null");
        Objects.requireNonNull(iter, "Iteration logic is null");
        Objects.requireNonNull(accessor, "Accessor is null");
        ListIterator<T> iterator = list.listIterator(0);
        int index = 0;
        while (iterator.hasNext()) {
            T next = iterator.next();
            SafeOpt<IterIterableResult<T>> tryVisit = accessor.tryVisit(index, next, iter);
            if (tryVisit.hasValueOrError()) {
                return tryVisit;
            }
            index++;

        }
        return SafeOpt.empty();
    }

    public static <T> SafeOpt<IterIterableResult<T>> find(T[] array, IterIterableAccessor accessor, IterIterableCons<T> iter) {
        Objects.requireNonNull(array, "Array is null");
        Objects.requireNonNull(iter, "Iteration logic is null");
        Objects.requireNonNull(accessor, "Accessor is null");
        for (int i = 0; i < array.length; i++) {
            SafeOpt<IterIterableResult<T>> tryVisit = accessor.tryVisit(i, array[i], iter);
            if (tryVisit.hasValueOrError()) {
                return tryVisit;
            }
        }
        return SafeOpt.empty();
    }

    public static <T> SafeOpt<IterIterableResult<T>> findBackwards(List<T> list, IterIterableAccessor accessor, IterIterableCons<T> iter) {
        Objects.requireNonNull(list, "List is null");
        Objects.requireNonNull(iter, "Iteration logic is null");
        Objects.requireNonNull(accessor, "Accessor is null");
        int size = list.size();
        ListIterator<T> iterator = list.listIterator(size);
        int index = size;
        while (iterator.hasPrevious()) {
            index--;
            T next = iterator.previous();
            SafeOpt<IterIterableResult<T>> tryVisit = accessor.tryVisit(index, next, iter);
            if (tryVisit.hasValueOrError()) {
                return tryVisit;
            }
        }
        return SafeOpt.empty();
    }

    public static <T> SafeOpt<IterIterableResult<T>> findBackwards(T[] array, IterIterableAccessor accessor, IterIterableCons<T> iter) {
        Objects.requireNonNull(array, "Array is null");
        Objects.requireNonNull(iter, "Iteration logic is null");
        Objects.requireNonNull(accessor, "Accessor is null");
        for (int i = array.length - 1; i >= 0; i--) {
            SafeOpt<IterIterableResult<T>> tryVisit = accessor.tryVisit(i, array[i], iter);
            if (tryVisit.hasValueOrError()) {
                return tryVisit;
            }
        }
        return SafeOpt.empty();
    }

    public static <T> SafeOpt<IterIterableResult<T>> findBackwards(Deque<T> deque, IterIterableAccessor accessor, IterIterableCons<T> iter) {
        Objects.requireNonNull(deque, "Deque is null");
        Objects.requireNonNull(iter, "Iteration logic is null");
        Objects.requireNonNull(accessor, "Accessor is null");
        int size = deque.size();
        Iterator<T> descendingIterator = deque.descendingIterator();
        int index = size - 1;
        while (descendingIterator.hasNext()) {
            T next = descendingIterator.next();

            SafeOpt<IterIterableResult<T>> tryVisit = accessor.tryVisit(index, next, iter);
            if (tryVisit.hasValueOrError()) {
                return tryVisit;
            }

            index--;

        }

        return SafeOpt.empty();
    }

    public static <K, V> SafeOpt<IterMapResult<K, V>> find(Map<K, V> map, IterMapAccessor accessor, IterMapCons<K, V> iter) {
        Objects.requireNonNull(map, "Map is null");
        Objects.requireNonNull(iter, "Iteration logic is null");
        Objects.requireNonNull(accessor, "Accessor is null");
        Iterator<Map.Entry<K, V>> iterator = map.entrySet().iterator();
        int index = 0;
        while (iterator.hasNext()) {
            Map.Entry<K, V> entry = iterator.next();
            SafeOpt<IterMapResult<K, V>> tryVisit = accessor.tryVisit(index, entry.getKey(), entry.getValue(), iter);
            if (tryVisit.hasValueOrError()) {
                return tryVisit;
            }
            index++;
        }
        return SafeOpt.empty();

    }

}
