/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lt.lb.commons.jpa.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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
 * @author Laimonas-Beniusis-PC
 */
public class ExtQueryImpl<X> implements ExtQuery<X> {

    private final EntityManager em;
    private final CriteriaBuilder cb;
    private final CriteriaQuery<X> q;
    private int first = -1;
    private int pageSize = -1;
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
        int size = 0;
        for (List l : lists) {
            size += l.size();
        }
        Predicate[] preds = new Predicate[size];
        int i = 0;
        for (List<Predicate> l : lists) {
            for (Predicate p : l) {
                preds[i++] = p;
            }
        }
        return preds;
    }

    @Override
    public List<X> getResultList() {
        q.orderBy(this.order);
        TypedQuery<X> finalQuery = em.createQuery(q.where(this.getPred(staticPredicates)));

        if (first > 0) {
            finalQuery = finalQuery.setFirstResult(first);
        }
        if (pageSize > 0) {
            finalQuery = finalQuery.setMaxResults(pageSize);
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
        this.pageSize = max;
        return this;
    }

    @Override
    public ExtQuery<X> setFirstResult(int first) {
        this.first = first;
        return this;
    }

};
