package lt.lb.commons.jpa.decorators;

import javax.persistence.criteria.Path;

/**
 *
 * @author laim0nas100
 */
public interface IOrderMaker<T> extends IQueryDecorator<T> {

    public OrderSort getOrderSort(Path<T> root);
}
