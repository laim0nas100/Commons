package lt.lb.commons.iteration.impl;

import java.util.ListIterator;
import java.util.Objects;
import lt.lb.commons.iteration.ReadOnlyBidirectionalIterator;

/**
 *
 * @author laim0nas100
 */
public class ListROI<T> extends BaseROI<T> implements ReadOnlyBidirectionalIterator<T> {

    protected ListIterator<T> iter;

    public ListROI(ListIterator<T> iter) {
        Objects.requireNonNull(iter);
        this.iter = iter;
    }

    @Override
    public boolean hasNext() {
        return iter.hasNext();
    }

    @Override
    public T next() {
        return setCurrentInc(iter.next());
    }

    @Override
    public boolean hasPrevious() {
        return iter.hasPrevious();
    }

    @Override
    public T previous() {
        return setCurrentDec(iter.previous());
    }

    @Override
    public int nextIndex() {
        return iter.nextIndex();
    }

    @Override
    public int previousIndex() {
        return iter.previousIndex();
    }

}
