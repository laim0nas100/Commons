package lt.lb.commons.iteration;

/**
 *
 * @author Laimonas Beniušis
 * @param <T>
 */
public interface Visitor<T> {
    public Boolean find(T item);
}
