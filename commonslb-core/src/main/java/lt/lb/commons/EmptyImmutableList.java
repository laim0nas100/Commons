package lt.lb.commons;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import org.apache.commons.collections4.iterators.EmptyListIterator;

/**
 * Does what it says in the name.
 * @author laim0nas100
 */
public class EmptyImmutableList<T> implements List<T> {
    
    private static final EmptyImmutableList empty = new EmptyImmutableList();
    
    /**
     * Get precomputed instance of such list
     * @param <T>
     * @return 
     */
    public static <T> EmptyImmutableList<T> getInstance(){
        return empty;
    }

    @Override
    public boolean add(T e) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public void add(int index, T element) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public boolean addAll(int index, Collection<? extends T> c) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public void clear() {
    }

    @Override
    public boolean contains(Object o) {
        return false;
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return false;
    }

    @Override
    public T get(int index) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public int indexOf(Object o) {
        return -1;
    }

    @Override
    public boolean isEmpty() {
        return true;
    }

    @Override
    public Iterator<T> iterator() {
        return listIterator();
    }

    @Override
    public int lastIndexOf(Object o) {
        return -1;
    }

    @Override
    public ListIterator<T> listIterator() {
        return EmptyListIterator.emptyListIterator();

    }

    @Override
    public ListIterator<T> listIterator(int index) {
        return listIterator();
    }

    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public T remove(int index) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public T set(int index, T element) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public List<T> subList(int fromIndex, int toIndex) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public Object[] toArray() {
        return ArrayOp.EMPTY_OBJECT_ARRAY;
    }

    @Override
    public <T> T[] toArray(T[] a) {
        if (a.length > 0) {
            a[0] = null;
        }
        return a;
    }

}
