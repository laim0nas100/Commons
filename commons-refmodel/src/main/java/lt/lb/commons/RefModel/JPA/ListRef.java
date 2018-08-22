/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lt.lb.commons.RefModel.JPA;

import javax.persistence.criteria.*;

/**
 *
 * @author Lemmin
 */
public class ListRef<T> extends SingularRef<T> {

    public <E> ListJoin<E, T> join(Root<E> root) {
        return this.join(root, JoinType.INNER);
    }

    public <E> ListJoin<E, T> join(Root<E> root, JoinType jt) {
        return root.joinList(this.local, jt);
    }

}
