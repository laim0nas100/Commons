/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lt.lb.commons.jpa.decorators;

import javax.persistence.criteria.Root;

/**
 *
 * @author laim0nas100
 */
public interface IOrderMaker<T> extends IQueryDecorator<T> {

    public OrderSort getOrderSort(Root<T> root);
}
