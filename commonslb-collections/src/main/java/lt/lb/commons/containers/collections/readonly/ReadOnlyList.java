package lt.lb.commons.containers.collections.readonly;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;
import java.util.function.UnaryOperator;

/**
 *
 * @author laim0nas100
 * @param <T>
 */
public interface ReadOnlyList<T> extends ReadOnlyCollection<T>, List<T> {

    @Override
    public default boolean add(T e) {
        throw new UnsupportedOperationException("Read only instance of " + getClass());
    }

    @Override
    public default boolean remove(Object o) {
        throw new UnsupportedOperationException("Read only instance of " + getClass());
    }

    @Override
    public default boolean addAll(Collection<? extends T> c) {
        throw new UnsupportedOperationException("Read only instance of " + getClass());
    }

    @Override
    public default boolean addAll(int index, Collection<? extends T> c) {
        throw new UnsupportedOperationException("Read only instance of " + getClass());
    }

    @Override
    public default boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException("Read only instance of " + getClass());
    }

    @Override
    public default boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException("Read only instance of " + getClass());
    }

    @Override
    public default void replaceAll(UnaryOperator<T> operator) {
        throw new UnsupportedOperationException("Read only instance of " + getClass());
    }

    @Override
    public default void sort(Comparator<? super T> c) {
        throw new UnsupportedOperationException("Read only instance of " + getClass());
    }

    @Override
    public default void clear() {
        throw new UnsupportedOperationException("Read only instance of " + getClass());
    }

    @Override
    public default T set(int index, T element) {
        throw new UnsupportedOperationException("Read only instance of " + getClass());
    }

    @Override
    public default void add(int index, T element) {
        throw new UnsupportedOperationException("Read only instance of " + getClass());
    }

    @Override
    public default T remove(int index) {
        throw new UnsupportedOperationException("Read only instance of " + getClass());
    }

    @Override
    public default int indexOf(Object o) {
        int i = -1;
        Iterator<T> it = iterator();
        while (it.hasNext()) {
            i++;
            if (Objects.equals(it.next(), o)) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public default int lastIndexOf(Object o) {
        int i = size();
        ListIterator<T> it = listIterator(i);
        while (it.hasPrevious()) {
            i--;
            if (Objects.equals(it.previous(), o)) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public default boolean isEmpty() {
        return ReadOnlyCollection.super.isEmpty();
    }

    @Override
    public default boolean containsAll(Collection<?> c) {
        return ReadOnlyCollection.super.containsAll(c);
    }

    @Override
    public default boolean contains(Object o) {
        return ReadOnlyCollection.super.contains(o);
    }

    @Override
    public default <T> T[] toArray(T[] a) {
        return ReadOnlyCollection.super.toArray(a);
    }

    @Override
    public default Object[] toArray() {
        return ReadOnlyCollection.super.toArray();
    }

}
