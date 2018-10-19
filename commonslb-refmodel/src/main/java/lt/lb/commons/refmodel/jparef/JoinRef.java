/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lt.lb.commons.refmodel.jparef;

import javax.persistence.criteria.From;
import javax.persistence.criteria.Path;
import lt.lb.commons.F;

/**
 *
 * @author laim0nas100
 */
public class JoinRef<T> extends SingularRef<T> {
    protected <E, A> From resolveJoin(From<E, A> root) {
        if(this.local.equals(this.relative)){
            return root;
        }else{
            Path<T> pathFrom = this.getPathFrom(root);
            return F.cast(pathFrom);
        }
    }
}
