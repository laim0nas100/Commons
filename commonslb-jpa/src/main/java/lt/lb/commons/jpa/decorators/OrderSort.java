/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lt.lb.commons.jpa.decorators;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Path;

/**
 *
 * @author laim0nas100
 */
public interface OrderSort {

    public default boolean needsMin() {
        return isAscending() == isNullFirst();
    }

    public boolean isAscending();

    public boolean isNullFirst();

    public boolean isNullable();

    public int getQueueOrder();

    public Order construct(EntityManager em, CriteriaQuery query, CriteriaBuilder cb);

    public interface OrderSortBuilder {


        public OrderSortBuilder setAscending(boolean asc);

        public OrderSortBuilder setNullable(boolean nullable);

        public OrderSortBuilder setNullFirst(boolean nullFirst);

        public OrderSortBuilder setQueueOrder(int order);

        public OrderSortBuilder setPath(Path path);

        public OrderSort build();
    }

}
