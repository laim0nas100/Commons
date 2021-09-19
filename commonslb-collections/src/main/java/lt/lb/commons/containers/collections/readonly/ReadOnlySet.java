package lt.lb.commons.containers.collections.readonly;

import java.util.Collection;
import java.util.Set;

/**
 *
 * @author laim0nas100
 * @param <T>
 */
public interface ReadOnlySet<T> extends ReadOnlyCollection<T>, Set<T> {

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
    public default boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException("Read only instance of " + getClass());
    }

    @Override
    public default boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException("Read only instance of " + getClass());
    }

    @Override
    public default void clear() {
        throw new UnsupportedOperationException("Read only instance of " + getClass());
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
