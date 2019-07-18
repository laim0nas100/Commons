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
        return iter.hasNext();
    }

    @Override
    public T next() {
        index++;
        return setCurrent(iter.next());
    }


}
