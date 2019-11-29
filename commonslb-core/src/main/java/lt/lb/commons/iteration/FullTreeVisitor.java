package lt.lb.commons.iteration;

/**
 *
 * Tree visitor, that visits all the elements, by returning false at each find request
 * @author laim0nas100
 */
public interface FullTreeVisitor<T> extends TreeVisitor<T>{

    @Override
    public default Boolean find(T item) {
        return false;
    }
    
}
