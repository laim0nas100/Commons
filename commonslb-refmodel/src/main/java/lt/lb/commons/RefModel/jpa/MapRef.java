/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lt.lb.commons.refmodel.jpa;

import javax.persistence.criteria.*;

/**
 *
 * @author Lemmin
 */
public class MapRef<K, T> extends SingularRef<T> {

    public <E> MapJoin<E, K, T> join(Root<E> root) {
        return this.join(root, JoinType.INNER);
    }

    public <E> MapJoin<E, K, T> join(Root<E> root, JoinType jt) {
        return root.joinMap(local, jt);
    }

}
