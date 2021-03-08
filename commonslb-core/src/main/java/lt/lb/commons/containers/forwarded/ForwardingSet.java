package lt.lb.commons.containers.forwarded;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.Spliterator;

/**
 *
 * @author laim0nas100
 */
public interface ForwardingSet<T> extends Set<T>, ForwardingCollection<T> {

    @Override
    public default int size() {
        return delegate().size();
    }

    @Override
    public default boolean isEmpty() {
        return delegate().isEmpty();
    }

    @Override
    public default boolean contains(Object o) {
        return delegate().contains(o);
    }

    @Override
    public default Iterator<T> iterator() {
        return delegate().iterator();
    }

    @Override
    public default Object[] toArray() {
        return delegate().toArray();
    }

    @Override
    public default <T> T[] toArray(T[] a) {
        return delegate().toArray(a);
    }

    @Override
    public default boolean add(T e) {
        return delegate().add(e);
    }

    @Override
    public default boolean remove(Object o) {
        return delegate().remove(o);
    }

    @Override
    public default boolean containsAll(Collection<?> c) {
        return delegate().containsAll(c);
    }

    @Override
    public default boolean addAll(Collection<? extends T> c) {
        return delegate().addAll(c);
    }

    @Override
    public default boolean retainAll(Collection<?> c) {
        return delegate().retainAll(c);
    }

    @Override
    public default boolean removeAll(Collection<?> c) {
        return delegate().removeAll(c);
    }

    @Override
    public default void clear() {
        delegate().clear();
    }

    @Override
    public default Spliterator<T> spliterator() {
        return delegate().spliterator();
    }

    @Override
    public Set<T> delegate();

}
