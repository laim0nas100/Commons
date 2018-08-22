/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lt.lb.commons.RefModel.JPA;

import java.util.regex.Matcher;
import javax.persistence.criteria.*;
import lt.lb.commons.RefModel.Ref;
import lt.lb.commons.RefModel.RefCompiler;

/**
 *
 * @author Lemmin
 */
public class SingularRef<T> extends Ref<T> {

    public Path<T> getPath(Path p) {
        String quoteReplacement = Matcher.quoteReplacement(RefCompiler.separator);
        String[] split = this.get().split(quoteReplacement);
//        String[] split = this.get().split("\\.");
        for (String path : split) {
            p = p.get(path);
        }
        return p;
    }

    public <E> Fetch<E, T> fetch(Root<E> root, JoinType jt) {
        return root.fetch(local, jt);
    }

    public <E> Fetch<E, T> fetch(Root<E> root) {
        return this.fetch(root, JoinType.INNER);
    }
}
