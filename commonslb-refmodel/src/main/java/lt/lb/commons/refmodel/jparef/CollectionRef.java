package lt.lb.commons.refmodel.jparef;

import javax.persistence.criteria.*;

/**
 *
 * @author laim0nas100
 */
public class CollectionRef<T> extends JoinRef<T> {

    public <E, A> CollectionJoin<E, T> join(From<E, A> root) {
        return this.join(root, JoinType.INNER);
    }

    public <E, A> CollectionJoin<E, T> join(From<E, A> root, JoinType jt) {
        return this.resolveJoin(root).joinCollection(this.local, jt);
    }

}
