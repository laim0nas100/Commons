/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lt.lb.commons.jpa.decorators;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Selection;

/**
 *
 * @author Laimonas-Beniusis-PC
 */
public class OrderSort {

        public boolean natural = false;
        public boolean ascending = true;
        public boolean nullFirst = false;
        public boolean careAboutNulls = true;
        public int priority;

        public Path path;

        private boolean needsMin() {
            return ascending == nullFirst;
        }

        public Order construct(EntityManager em, CriteriaQuery query, CriteriaBuilder cb) {
            Expression exp = path;
            if (this.careAboutNulls) {
                if (this.needsMin()) {
                    exp = cb.min(path);
                } else {
                    exp = cb.max(path);
                }
                Selection selection = query.getSelection();//save selection
                query.select(exp);
                Object substitute = em.createQuery(query).getSingleResult();
                query.select(selection);//restore selection

                Expression coalesce = cb.coalesce(path, substitute);
                exp = coalesce;
            }

            if (ascending) {
                return cb.asc(exp);
            } else {
                return cb.desc(exp);
            }
        }

        @Override
        public String toString() {
            return "natural:" + natural + " careAboutNulls:" + careAboutNulls + " nullFirst" + nullFirst + " ascending:" + ascending + " priority:" + priority;
        }

    }
