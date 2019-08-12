package lt.lb.commons.iteration.impl;

import java.util.NoSuchElementException;
import lt.lb.commons.iteration.ReadOnlyBidirectionalIterator;
import lt.lb.commons.iteration.ReadOnlyIterator;

/**
 *
 * @author laim0nas100
 */
public class EmptyROI<T> implements ReadOnlyBidirectionalIterator<T>{

    @Override
    public boolean hasNext() {
        return false;
    }

    @Override
    public T next() {
        throw new NoSuchElementException("No next value");
    }

    @Override
    public Integer getCurrentIndex() {
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
    
}
