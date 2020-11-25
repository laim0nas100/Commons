package lt.lb.commons.iteration;

/**
 *
 * @author laim0nas100
 * @param <T>
 */
public interface Visitor<T> {
    /**
     * 
     * @param item
     * @return Whether to terminate search.
     */
    public Boolean find(T item);
}
