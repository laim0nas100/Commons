package lt.lb.commons.containers.forwarded;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Spliterator;

/**
 *
 * @author laim0nas100
 */
public interface ForwardingList<T> extends List<T>, ForwardingCollection<T> {

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
    public default boolean addAll(int index, Collection<? extends T> c) {
        return delegate().addAll(index, c);
    }

    @Override
    public default boolean removeAll(Collection<?> c) {
        return delegate().removeAll(c);
    }

    @Override
    public default boolean retainAll(Collection<?> c) {
        return delegate().retainAll(c);
    }

    @Override
    public default void clear() {
        delegate().clear();
    }

    @Override
    public default T get(int index) {
        return delegate().get(index);
    }

    @Override
    public default T set(int index, T element) {
        return delegate().set(index, element);
    }

    @Override
    public default void add(int index, T element) {
        delegate().add(index, element);
    }

    @Override
    public default T remove(int index) {
        return delegate().remove(index);
    }

    @Override
    public default int indexOf(Object o) {
        return delegate().indexOf(o);
    }

    @Override
    public default int lastIndexOf(Object o) {
        return delegate().lastIndexOf(o);

    }

    @Override
    public default ListIterator<T> listIterator() {
        return delegate().listIterator();
    }

    @Override
    public default ListIterator<T> listIterator(int index) {
        return delegate().listIterator(index);
    }

    @Override
    public default List<T> subList(int fromIndex, int toIndex) {
        return delegate().subList(fromIndex, toIndex);
    }

    @Override
    public List<T> delegate();

    @Override
    public default Spliterator<T> spliterator() {
        return delegate().spliterator();
    }

    @Override
    public default Object[] toArray() {
        return delegate().toArray();
    }

    @Override
    public default <T> T[] toArray(T[] a) {
        return delegate().toArray(a);
    }

}
