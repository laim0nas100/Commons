package lt.lb.commons.refmodel.jparef;

import javax.persistence.criteria.*;

/**
 *
 * @author laim0nas100
 */
public class JpaMapRef<K, T> extends JpaJoinRef<T> {

    
    public <E,A> MapJoin<E, K, T> join(From<E,A> root) {
        return join(root, JoinType.INNER);
    }

    public <E,A> MapJoin<E, K, T> join(From<E,A> root, JoinType jt) {
        return resolveJoin(root).joinMap(getLocal(), jt);
    }

}
