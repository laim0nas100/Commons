package lt.lb.commons.refmodel.jparef;

import javax.persistence.criteria.*;

/**
 *
 * @author laim0nas100
 */
public class ListRef<T> extends JoinRef<T> {

    public <E,A> ListJoin<E, T> join(From<E,A> root) {
        return this.join(root, JoinType.INNER);
    }

    public <E,A> ListJoin<E, T> join(From<E,A> root, JoinType jt) {
        return this.resolveJoin(root).joinList(this.local, jt);
    }

}
