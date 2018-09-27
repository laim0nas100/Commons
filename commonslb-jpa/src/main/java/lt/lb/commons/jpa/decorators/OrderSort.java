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

    public static class OrderSortBuidler {

        private DefaultOrderSort sort = new DefaultOrderSort();

        public OrderSortBuidler setAscending(boolean asc) {
            sort.ascending = asc;
            return this;
        }

        public OrderSortBuidler setNullable(boolean nullable) {
            sort.nullable = nullable;
            return this;
        }

        public OrderSortBuidler setNullFirst(boolean nullFirst) {
            sort.nullFirst = nullFirst;
            return this;
        }

        public OrderSortBuidler setQueueOrder(int order) {
            sort.queueOrder = order;
            return this;
        }

        public OrderSortBuidler setPath(Path path) {
            sort.path = path;
            return this;
        }

        public OrderSort build() {
            return sort;
        }
    }

}
