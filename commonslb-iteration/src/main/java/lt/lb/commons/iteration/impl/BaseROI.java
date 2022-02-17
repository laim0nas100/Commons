package lt.lb.commons.iteration.impl;

import lt.lb.commons.iteration.ReadOnlyIterator;

/**
 *
 * @author laim0nas100
 */
public abstract class BaseROI<T> implements ReadOnlyIterator<T> {

    protected int index = -1;
    protected T current;
    protected boolean closed = false;

    protected void assertClosed() {
        if (closed) {
            throw new IllegalStateException("Closed!");
        }
    }

    @Override
    public void close() {
        closed = true;
    }
    
    

    @Override
    public int getCurrentIndex() {
        return index;
    }

    @Override
    public T getCurrent() {
        return current;
    }

    protected T setCurrent(T val) {
        current = val;
        return current;
    }

    protected T setCurrentInc(T val) {
        index++;
        return setCurrent(val);
    }

    protected T setCurrentDec(T val) {
        index--;
        return setCurrent(val);
    }

}
