package com.github.laim0nas100.commonslb.jpa;

import java.util.List;

/**
 *
 * @author laim0nas100
 * @param <X>
 */
public interface ExtQuery<X> {

    List<X> getResultList();

    X getSingleResult();

    ExtQuery<X> setMaxResults(int max);

    ExtQuery<X> setFirstResult(int first);

}
