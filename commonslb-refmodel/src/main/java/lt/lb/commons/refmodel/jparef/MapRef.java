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
public class MapRef<K, T> extends JoinRef<T> {

    public <E> MapJoin<E, K, T> join(From<E,T> root) {
        return this.join(root, JoinType.INNER);
    }

    public <E> MapJoin<E, K, T> join(From<E,T> root, JoinType jt) {
        return root.joinMap(local, jt);
    }

}
