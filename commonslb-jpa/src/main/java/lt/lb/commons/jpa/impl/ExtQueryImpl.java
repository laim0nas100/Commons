package lt.lb.commons.jpa.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import lt.lb.commons.jpa.ExtQuery;
import lt.lb.commons.jpa.decorators.IOrderMaker;
import lt.lb.commons.jpa.decorators.IPredicateMaker;
import lt.lb.commons.jpa.decorators.IQueryDecorator;
import lt.lb.commons.jpa.decorators.OrderSort;

/**
 *
 * @author laim0nas100
 */
public class ExtQueryImpl<X> implements ExtQuery<X> {
    
    private final EntityManager em;
    private final CriteriaBuilder cb;
    private final CriteriaQuery<X> q;
    private int first = -1;
    private int maxResults = -1;
    private List<Predicate> staticPredicates = new ArrayList<>();
    private List<Order> order = new ArrayList<>();
    
    public ExtQueryImpl(EntityManager manager, CriteriaBuilder builder, CriteriaQuery<X> q, Root<X> root, IQueryDecorator<X>... decorators) {
        em = manager;
        cb = builder;
        this.q = q;
        
        List<OrderSort> cool = new ArrayList<>();
        
        for (IQueryDecorator dec : decorators) {
            if (dec instanceof IPredicateMaker) {
                Predicate make = ((IPredicateMaker) dec).make(cb, root);
                this.staticPredicates.add(make);
                
            } else if (dec instanceof IOrderMaker) {
                IOrderMaker maker = (IOrderMaker) dec;
                cool.add(maker.getOrderSort(root));
            } else {
                throw new IllegalArgumentException(dec.getClass() + " decorator is not supported");
            }
        }
        Collections.sort(cool, (OrderSort o1, OrderSort o2) -> {
            return o1.getQueueOrder() - o2.getQueueOrder();
            
        });
        for (OrderSort sort : cool) {
            order.add(sort.construct(em, q, cb));
        }
        
    }
    
    private Predicate[] getPred(List<Predicate>... lists) {
        return Stream.of(lists).flatMap(l -> l.stream()).toArray(s -> new Predicate[s]);
    }
    
    @Override
    public List<X> getResultList() {
        q.orderBy(this.order);
        TypedQuery<X> finalQuery = em.createQuery(q.where(this.getPred(staticPredicates)));
        
        if (first > 0) {
            finalQuery = finalQuery.setFirstResult(first);
        }
        if (maxResults > 0) {
            finalQuery = finalQuery.setMaxResults(maxResults);
        }
        return finalQuery.getResultList();
        
    }
    
    @Override
    public X getSingleResult() {
        CriteriaQuery<X> where = q.where(this.getPred(staticPredicates));
        return em.createQuery(where).getSingleResult();
    }
    
    @Override
    public ExtQuery<X> setMaxResults(int max) {
        this.maxResults = max;
        return this;
    }
    
    @Override
    public ExtQuery<X> setFirstResult(int first) {
        this.first = first;
        return this;
    }
    
};
