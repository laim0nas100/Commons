package lt.lb.commons.jpa.tuple;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Selection;
import lt.lb.commons.F;
import lt.lb.commons.Nulls;
import lt.lb.commons.containers.collections.ImmutableCollections;
import lt.lb.commons.jpa.querydecor.DecoratorPhases;
import lt.lb.commons.jpa.querydecor.JpaExpResolve;

/**
 *
 * @author laim0nas100
 */
public class TupleProjectionSpec<R> implements TupleProjectionSpecList<R,Object> {
    
    private List<JpaExpResolve<R, ?, ? extends Path<R>, ? extends Expression<?>,?>> list;
    private List<Selection<?>> selections;
    
    public TupleProjectionSpec(JpaExpResolve<R, ?, ? extends Path<R>, ?,?>... s) {
        Nulls.requireNonNulls((Object[]) s);
        this.list = ImmutableCollections.listOf(s);
    }
    
    public TupleProjectionSpec(List<JpaExpResolve<R, ?, Path<R>, Expression<?>,?>> s) {
        this.list = Collections.unmodifiableList(s);
    }
    
    @Override
    public <C> List<Selection<?>> getAllSelections(DecoratorPhases.Phase3Abstract<R, ?, C> from) {
        if (selections == null) {
            final Root<R> root = from.root();
            selections = new ArrayList<>(list.size());
            list.forEach(exp -> {
                selections.add(exp.apply(F.cast(from),F.cast(root)));
            });
        }
        
        return selections;
    }
    
    @Override
    public List getList() {
        return ImmutableCollections.listOf();
    }
    
}
