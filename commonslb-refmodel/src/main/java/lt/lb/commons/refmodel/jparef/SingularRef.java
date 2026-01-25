package lt.lb.commons.refmodel.jparef;

import javax.persistence.criteria.*;
import lt.lb.commons.refmodel.Ref;
import lt.lb.commons.refmodel.RefCompiler;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author laim0nas100
 */
public class SingularRef<T> extends Ref<T> {
    
    public Path<T> getPathFrom(Path p) {
        String str = getRelative();
        String[] split = StringUtils.split(str, RefCompiler.DEFAULT_SEPARATOR);
        for (String path : split) {
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
