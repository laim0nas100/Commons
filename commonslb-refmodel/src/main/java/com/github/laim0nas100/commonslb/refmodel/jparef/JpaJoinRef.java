package com.github.laim0nas100.commonslb.refmodel.jparef;

import javax.persistence.criteria.From;
import javax.persistence.criteria.Path;
import com.github.laim0nas100.commonslb.F;

/**
 *
 * @author laim0nas100
 */
public class JpaJoinRef<T> extends SingularRef<T> {
    protected <E, A> From resolveJoin(From<E, A> root) {
        if(getLocal().equals(getRelative())){
            return root;
        }else{
            Path<T> pathFrom = this.getPathFrom(root);
            return F.cast(pathFrom);
        }
    }
}
