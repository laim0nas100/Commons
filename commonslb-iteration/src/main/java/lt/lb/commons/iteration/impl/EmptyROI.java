package lt.lb.commons.iteration.impl;

import java.util.ListIterator;
import java.util.NoSuchElementException;
import lt.lb.commons.iteration.ReadOnlyBidirectionalIterator;

/**
 *
 * @author laim0nas100
 */
public class EmptyROI<T> implements ReadOnlyBidirectionalIterator<T>, ListIterator<T> {

    @Override
    public boolean hasNext() {
        return false;
    }

    @Override
    public T next() {
        throw new NoSuchElementException("No next value");
    }

    @Override
    public int getCurrentIndex() {
        return -1;
    }

    @Override
    public T getCurrent() {
        throw new NoSuchElementException("No current value");
    }

    @Override
    public boolean hasPrevious() {
        return false;
    }

    @Override
    public T previous() {
        throw new NoSuchElementException("No previous value");
    }

    @Override
    public int nextIndex() {
        return -1;
    }

    @Override
    public int previousIndex() {
        return -1;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public void set(Object e) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public void add(Object e) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public void close() {
    }

}
