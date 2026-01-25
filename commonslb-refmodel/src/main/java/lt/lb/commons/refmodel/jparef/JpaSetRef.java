package lt.lb.commons.refmodel.jparef;

import javax.persistence.criteria.*;

/**
 *
 * @author laim0nas100
 */
public class JpaSetRef<T> extends JpaJoinRef<T> {
    
    public <E,A> SetJoin<E, T> join(From<E,A> root) {
        return join(root, JoinType.INNER);
    }

    public <E,A> SetJoin<E, T> join(From<E,A> root, JoinType jt) {
        return resolveJoin(root).joinSet(getLocal(), jt);
    }

}
