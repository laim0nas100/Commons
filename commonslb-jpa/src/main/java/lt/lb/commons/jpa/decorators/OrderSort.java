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
    
}
