package lt.lb.commons.refmodel;

/**
 * @author laim0nas100 marker interface when Ref is composite and is a
 * list/array
 */
public interface RefList<T> {

    /**
     * List access.
     * <p>
     * Use index = size() or -1 to point to the non-existing position after the
     * last element (for appending). Reading from that position is not
     * supported.
     * <p>
     * Negative indices are offset by one:
     * <br> positive: [0, 1, 2, 3, 4] [5=append]
     * <br> negative: [-6=first, -5, -4, -3, -2=last] [-1=append]
     * <p>
     * To normalize negative indices you can use:
     * <pre>{@code i = (i < 0) ? i + size() + 1 : i}</pre>
     *
     * @param i index
     * @return typed reference to the element at given position
     */
    T at(int i);
}
