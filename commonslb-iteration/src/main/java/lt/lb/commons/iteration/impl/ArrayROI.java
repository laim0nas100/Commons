package lt.lb.commons.iteration.impl;

import java.util.NoSuchElementException;
import java.util.Objects;
import lt.lb.commons.iteration.ReadOnlyBidirectionalIterator;

/**
 *
 * @author laim0nas100
 */
public class ArrayROI<T> extends BaseROI<T> implements ReadOnlyBidirectionalIterator<T> {

    protected final T[] array;

    public ArrayROI(T... array) {
        Objects.requireNonNull(array);
        this.array = array;
    }

    @Override
    public boolean hasPrevious() {
        if (closed) {
            return false;
        }
        return index > 0;
    }

    @Override
    public T previous() {
        assertClosed();
        if (!hasPrevious()) {
            throw new NoSuchElementException("No previous value");
        }
        return setCurrent(array[--index]);
    }

    @Override
    public boolean hasNext() {
        if (closed) {
            return false;
        }
        return 1 + index < array.length;
    }

    @Override
    public T next() {
        assertClosed();
        if (!hasNext()) {
            throw new NoSuchElementException("No next value");
        }
        return setCurrent(array[++index]);
    }

    @Override
    public int nextIndex() {
        if (hasNext()) {
            return index + 1;
        } else {
            return -1;
        }
    }

    @Override
    public int previousIndex() {
        if (hasPrevious()) {
            return index - 1;
        } else {
            return -1;
        }
    }

}
