/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lt.lb.commons.jpa;

import java.util.List;

/**
 *
 * @author Laimonas-Beniusis-PC
 */
public interface ExtQuery<X> {

    List<X> getResultList();

    X getSingleResult();

    ExtQuery<X> setMaxResults(int max);

    ExtQuery<X> setFirstResult(int first);

}
