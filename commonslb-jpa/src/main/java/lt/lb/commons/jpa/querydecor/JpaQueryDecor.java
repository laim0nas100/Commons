package lt.lb.commons.jpa.querydecor;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import javax.persistence.Tuple;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Selection;
import javax.persistence.metamodel.SingularAttribute;
import lt.lb.commons.F;
import lt.lb.commons.jpa.querydecor.DecoratorPhases.Phase2;

/**
 *
 * @author laim0nas100
 */
public class JpaQueryDecor<T_ROOT, T_RESULT> extends BaseJpaQueryDecor<T_ROOT, T_RESULT, JpaQueryDecor<T_ROOT, T_RESULT>> {

    protected JpaQueryDecor(Class<T_ROOT> rootClass, Class<T_RESULT> resultClass, JpaQueryDecor copy) {
        super(copy);
        this.rootClass = Objects.requireNonNull(rootClass);
        this.resultClass = Objects.requireNonNull(resultClass);
    }

    protected JpaQueryDecor(JpaQueryDecor copy) {
        this(copy.rootClass, copy.rootClass, copy);
    }

    public static <T> JpaQueryDecor<T, T> of(Class<T> root) {
        JpaQueryDecor<T, T> decor = new JpaQueryDecor<>(root, root, null);
        decor.selection = Phase2::root;
        return decor;
    }

    @Override
    protected JpaQueryDecor<T_ROOT, T_RESULT> me() {
        return new JpaQueryDecor<>(this);
    }

    @Override
    public <RES> JpaQueryDecor<T_ROOT, RES> selecting(Class<RES> resClass, Function<Phase2<T_ROOT>, Expression<RES>> func) {
        JpaQueryDecor<T_ROOT, RES> of = new JpaQueryDecor<>(rootClass, resClass, this);
        of.selection = func;
        return of;
    }

    @Override
    public <RES> JpaQueryDecor<T_ROOT, RES> selecting(SingularAttribute<T_ROOT, RES> att) {
        return F.cast(super.selecting(att));
    }

    @Override
    public JpaQueryDecor<T_ROOT,Tuple> selectingTuple(Function<Phase2<T_ROOT>, List<Selection<?>>> selections) {
        JpaQueryDecor<T_ROOT, Tuple> of = new JpaQueryDecor<>(rootClass, Tuple.class, this);
        of.multiselection = selections;
        return of;
    }

    @Override
    public JpaQueryDecor<T_ROOT,Tuple> selectingTuple(Selection<?>... selections) {
        return F.cast(super.selectingTuple(selections)); 
    }

    @Override
    public JpaQueryDecor<T_ROOT,Tuple> selectingTuple(List<Selection<?>> selections) {
        return F.cast(super.selectingTuple(selections));
    }
    
    

}
