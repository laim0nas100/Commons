/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lt.lb.commons.jpa.decorators;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

/**
 *
 * @author Laimonas-Beniusis-PC
 */
public interface IPredicateMaker<T> extends IQueryDecorator<T> {

    public Predicate make(CriteriaBuilder cb, Root<T> root);

}
