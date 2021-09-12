package lt.lb.commons.jpa.querydecor.based;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import javax.persistence.Tuple;
import javax.persistence.criteria.AbstractQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Selection;
import javax.persistence.criteria.Subquery;
import javax.persistence.metamodel.SingularAttribute;
import lt.lb.commons.jpa.querydecor.DecoratorPhases;

/**
 *
 * @author laim0nas100
 */
public interface IAbstractQueryDecor<T_ROOT, T_RESULT, CTX, M extends IAbstractQueryDecor<T_ROOT, T_RESULT, CTX, M>> extends ICommonRootQuery<T_ROOT, CTX, M> {

    public Class<T_RESULT> getResultClass();

    public M withPred(boolean having, Function<DecoratorPhases.Phase2<T_ROOT, CTX>, Predicate> func);

    public M withPredAbstract(boolean having, Function<DecoratorPhases.Phase3Abstract<T_ROOT, T_RESULT, CTX>, Predicate> func);

    public M withDec3Abstract(Consumer<DecoratorPhases.Phase3Abstract<T_ROOT, T_RESULT, CTX>> cons);

    @Override
    public default M withPred(Function<DecoratorPhases.Phase2<T_ROOT, CTX>, Predicate> func) {
        return withPred(false, func);
    }

    public default M withPredAny(Function<DecoratorPhases.Phase3Abstract<T_ROOT, T_RESULT, CTX>, Predicate> func) {
        return withPredAbstract(false, func);
    }

    public <RES> IAbstractQueryDecor<T_ROOT, RES, CTX, ?> selecting(
            Class<RES> resClass, Function<DecoratorPhases.Phase2<T_ROOT, CTX>, Expression<RES>> func);

    public IAbstractQueryDecor<T_ROOT, Tuple, CTX, ?> selectingTuple(Function<DecoratorPhases.Phase2<T_ROOT, CTX>, List<Selection<?>>> selections);

    public default IAbstractQueryDecor<T_ROOT, T_ROOT, CTX, ?> selectingRoot() {
        return selecting(getRootClass(), p -> p.root());
    }

    public default <RES> IAbstractQueryDecor<T_ROOT, RES, CTX, ?> selecting(
            SingularAttribute<? super T_ROOT, RES> att) {
        Objects.requireNonNull(att);
        Class<RES> res = att.getJavaType();
        return selecting(res, p -> p.root().get(att));
    }

    public default IAbstractQueryDecor<T_ROOT, Tuple, CTX, ?> selectingTuple(SingularAttribute<? super T_ROOT, ?>... selections) {
        for (SingularAttribute<? super T_ROOT, ?> att : selections) {
            Objects.requireNonNull(att);
        }
        return selectingTuple(p -> {
            Root<T_ROOT> root = p.root();
            List<Selection<?>> sel = new ArrayList<>(selections.length);
            for (SingularAttribute<? super T_ROOT, ?> att : selections) {
                sel.add(root.get(att));
            }
            return sel;
        });
    }

    public default IAbstractQueryDecor<T_ROOT, Long, CTX, ?> selectingCount() {
        return selecting(Long.class, c -> c.cb().count(c.root()));
    }

    public default <RES> IAbstractQueryDecor<T_ROOT, Long, CTX, ?> selectingCount(SingularAttribute<? super T_ROOT, RES> att) {
        Objects.requireNonNull(att);
        return selecting(Long.class, c -> c.cb().count(c.root().get(att)));
    }

    public default IAbstractQueryDecor<T_ROOT, Long, CTX, ?> selectingCountDistinct() {
        return selecting(Long.class, c -> c.cb().countDistinct(c.root()));
    }

    public default <RES> IAbstractQueryDecor<T_ROOT, Long, CTX, ?> selectingCountDistinct(SingularAttribute<? super T_ROOT, RES> att) {
        Objects.requireNonNull(att);
        return selecting(Long.class, c -> c.cb().countDistinct(c.root().get(att)));
    }

    public default <RES> M withSubquery(ISubqueryDecor<T_ROOT, RES, CTX, ?> decor, Function<DecoratorPhases.Phase3Subquery<T_ROOT, RES, CTX>, Predicate> func) {
        Objects.requireNonNull(decor);
        Objects.requireNonNull(func);

        return withPredAny(t -> {
            AbstractQuery<T_RESULT> query = t.query();
            Subquery<RES> subquery = query.subquery(decor.getResultClass());
            subquery = decor.produceSubquery(t.em(), subquery);
            DecoratorPhases.Phase3Subquery<T_ROOT, RES, CTX> p3 = DecoratorPhases.of(t.em(), t.cb(), t.root(), subquery, getContext());
            return func.apply(p3);
        });
    }

    public default <RES> M withSubquery(ISubqueryDecor<T_ROOT, RES, CTX, ?> decor, Consumer<DecoratorPhases.Phase3Subquery<T_ROOT, RES, CTX>> cons) {
        Objects.requireNonNull(decor);
        Objects.requireNonNull(cons);
        return withDec3Abstract(t -> {
            AbstractQuery<T_RESULT> query = t.query();
            Subquery<RES> subquery = query.subquery(decor.getResultClass());
            subquery = decor.produceSubquery(t.em(), subquery);
            DecoratorPhases.Phase3Subquery<T_ROOT, RES, CTX> p3 = DecoratorPhases.of(t.em(), t.cb(), t.root(), subquery, query, getContext());
            cons.accept(p3);
        });
    }

    public default M setDistinct(final boolean distinct) {
        return withDec3Abstract(c -> c.query().distinct(distinct));
    }

    public default M setGroupBy(Function<DecoratorPhases.Phase3Abstract<T_ROOT, T_RESULT, CTX>, List<Expression<?>>> func) {
        Objects.requireNonNull(func);
        return withDec3Abstract(c -> c.query().groupBy(func.apply(c)));
    }

}
