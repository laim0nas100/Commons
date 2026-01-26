package lt.lb.commons.refmodel.jparef;

import javax.persistence.criteria.*;
import lt.lb.commons.refmodel.Ref;

/**
 *
 * @author laim0nas100
 */
public class SingularRef<T> extends Ref<T> {
    
    public Path<T> getPathFrom(Path p) {
        for (String path : steps()) {
            p = p.get(path);
        }
        return p;
    }

    public <A,B> Fetch<A,T> fetch(FetchParent<A,B> root, JoinType jt) {
        return root.fetch(getLocal(), jt);
    }

    public <A,B> Fetch<A,T> fetch(FetchParent<A,B> root) {
        return fetch(root, JoinType.INNER);
    }
}
