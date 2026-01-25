package lt.lb.commons.refmodel.jparef;

import javax.persistence.criteria.From;
import javax.persistence.criteria.Path;
import lt.lb.commons.F;

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
