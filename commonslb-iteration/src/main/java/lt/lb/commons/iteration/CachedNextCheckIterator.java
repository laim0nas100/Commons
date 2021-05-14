package lt.lb.commons.iteration;

import java.util.Iterator;
import java.util.Objects;

/**
 * Iterator implementation with cached {@link Iterator#hasNext} operation, to
 * prevent repeated calls.
 *
 * @author laim0nas100
 */
public class CachedNextCheckIterator<T> implements Iterator<T> {

    protected Iterator<T> ite;
    protected Boolean hasNext = null;

    public CachedNextCheckIterator(Iterator<T> ite) {
        this.ite = Objects.requireNonNull(ite, "Iterator must not be null");
    }

    @Override
    public boolean hasNext() {
        if (hasNext == null) {
            hasNext = ite.hasNext();
        }
        return hasNext;
    }

    @Override
    public T next() {
        hasNext = null;
        T next = ite.next();
        return next;
    }

    @Override
    public void remove() {
        hasNext = null;
        ite.remove();
    }

}
