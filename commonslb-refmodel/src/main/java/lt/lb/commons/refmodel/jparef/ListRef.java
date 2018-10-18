/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lt.lb.commons.refmodel.jparef;

import javax.persistence.criteria.*;

/**
 *
 * @author laim0nas100
 */
public class ListRef<T> extends JoinRef<T> {

    public <E> ListJoin<E, T> join(From<E,T> root) {
        return this.join(root, JoinType.INNER);
    }

    public <E> ListJoin<E, T> join(From<E,T> root, JoinType jt) {
        return root.joinList(this.local, jt);
    }

}
