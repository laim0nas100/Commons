package lt.lb.commons.iteration.impl;

import java.util.Iterator;
import java.util.Objects;

/**
 *
 * @author laim0nas100
 */
public class IteratorROI<T> extends BaseROI<T> {

    protected Iterator<T> iter;

    public IteratorROI(Iterator<T> iter) {
        Objects.requireNonNull(iter);
        this.iter = iter;
    }

    @Override
    public boolean hasNext() {
        if(closed){
            return false;
        }
        return iter.hasNext();
    }

    @Override
    public T next() {
        assertClosed();
        return setCurrentInc(iter.next());
    }

}
