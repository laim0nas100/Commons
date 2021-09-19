package lt.lb.commons.containers.collections.readonly;

import java.util.Collection;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 *
 * @author laim0nas100
 * @param <T>
 */
public interface ReadOnlyCollection<T> extends Collection<T> {

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
    public default boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException("Read only instance of " + getClass());
    }

    @Override
    public default boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException("Read only instance of " + getClass());
    }

    @Override
    public default void clear() {
        throw new UnsupportedOperationException("Read only instance of " + getClass());
    }

    @Override
    public default boolean removeIf(Predicate<? super T> filter) {
        throw new UnsupportedOperationException("Read only instance of " + getClass());
    }

    @Override
    public default Object[] toArray() {
        return StreamSupport.stream(spliterator(), false).toArray();
    }

    @Override
    public default <T> T[] toArray(T[] a) {
        return StreamSupport.stream(spliterator(), false).collect(Collectors.toList()).toArray(a);
    }

    @Override
    public default boolean contains(Object o) {
        return stream().filter(item -> Objects.equals(item, o)).findAny().isPresent();
    }

    @Override
    public default boolean containsAll(Collection<?> c) {
        return stream().collect(Collectors.toList()).containsAll(c);
    }

    @Override
    public default boolean isEmpty() {
        return size() == 0;
    }

}
