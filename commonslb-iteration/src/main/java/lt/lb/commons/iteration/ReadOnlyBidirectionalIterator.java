package lt.lb.commons.iteration;

import java.util.ListIterator;

/**
 *
 * @author laim0nas100
 */
public interface ReadOnlyBidirectionalIterator<T> extends ReadOnlyIterator<T>, ListIterator<T> {

    @Override
    public boolean hasPrevious();

    @Override
    public T previous();

    /**
     * Not supported
     */
    @Override
    public default void set(T e) {
        throw new UnsupportedOperationException("set");
    }

    /**
     * Not supported
     */
    @Override
    public default void add(T e) {
        throw new UnsupportedOperationException("add");
    }

    /**
     * Not supported
     */
    @Override
    public default void remove() {
        throw new UnsupportedOperationException("remove");
    }

}
