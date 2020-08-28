package lt.lb.commons.refmodel.jparef;

import javax.persistence.criteria.*;

/**
 *
 * @author laim0nas100
 */
public class MapRef<K, T> extends JoinRef<T> {

    
    public <E,A> MapJoin<E, K, T> join(From<E,A> root) {
        return this.join(root, JoinType.INNER);
    }

    public <E,A> MapJoin<E, K, T> join(From<E,A> root, JoinType jt) {
        return this.resolveJoin(root).joinMap(local, jt);
    }

}
