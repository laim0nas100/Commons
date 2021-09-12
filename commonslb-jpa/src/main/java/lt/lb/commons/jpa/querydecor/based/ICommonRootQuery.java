package lt.lb.commons.jpa.querydecor.based;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import javax.persistence.FlushModeType;
import javax.persistence.LockModeType;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Subquery;
import javax.persistence.metamodel.MapAttribute;
import javax.persistence.metamodel.PluralAttribute;
import javax.persistence.metamodel.SingularAttribute;
import lt.lb.commons.jpa.querydecor.DecoratorPhases;

/**
 *
 * @author laim0nas100
 * @param <T_ROOT>
 * @param <CTX>
 * @param <M>
 */
public interface ICommonRootQuery<T_ROOT, CTX, M extends ICommonRootQuery<T_ROOT, CTX, M>> {

    public abstract <NEW_ROOT extends T_ROOT> ICommonRootQuery<NEW_ROOT, CTX, ?> usingSubtype(Class<NEW_ROOT> subtype);

    public Class<T_ROOT> getRootClass();

    public CTX getContext();

    public M withPred(Function<DecoratorPhases.Phase2<T_ROOT, CTX>, Predicate> func);

    public M withPredCommon(Function<DecoratorPhases.Phase3Common<T_ROOT, CTX>, Predicate> func);

    public M withDec1(Consumer<DecoratorPhases.Phase1<CTX>> cons);

    public M withDec2(Consumer<DecoratorPhases.Phase2<T_ROOT, CTX>> cons);

    public M withDec3Common(Consumer<DecoratorPhases.Phase3Common<T_ROOT, CTX>> cons);

    public M withDec4(Consumer<DecoratorPhases.Phase4<CTX>> cons);

    public default <T> M withPred(SingularAttribute<? super T_ROOT, T> att, BiFunction<CriteriaBuilder, Expression<T>, Predicate> func) {
        Objects.requireNonNull(att);
        Objects.requireNonNull(func);
        return withPred(t -> func.apply(t.cb(), t.root().get(att)));
    }

    public default <T, C extends Collection<T>> M withPred(PluralAttribute<T_ROOT, C, T> att, BiFunction<CriteriaBuilder, Expression<C>, Predicate> func) {
        Objects.requireNonNull(att);
        Objects.requireNonNull(func);
        return withPred(t -> func.apply(t.cb(), t.root().get(att)));
    }

    public default <K, V, MAP extends Map<K, V>> M withPred(MapAttribute<T_ROOT, K, V> att, BiFunction<CriteriaBuilder, Expression<MAP>, Predicate> func) {
        Objects.requireNonNull(att);
        Objects.requireNonNull(func);
        return withPred(t -> func.apply(t.cb(), t.root().get(att)));
    }

    public default <RES> M withSubqueryPred(ISubqueryDecor<T_ROOT, RES, CTX, ?> decor, Function<Subquery<RES>, Predicate> func) {
        Objects.requireNonNull(decor);
        Objects.requireNonNull(func);

        return withPredCommon(t -> {
            Subquery<RES> subquery = t.query().subquery(decor.getResultClass());
            subquery = decor.produceSubquery(t.em(), subquery);
            return func.apply(subquery);
        });
    }

    public default M setMaxResults(int max) {
        return withDec4(p -> p.query().setMaxResults(max));
    }

    public default M setFirstResult(int startPosition) {
        return withDec4(p -> p.query().setFirstResult(startPosition));
    }

    public default M setLockMode(LockModeType lock) {
        return withDec4(p -> p.query().setLockMode(lock));
    }

    public default M setFlushMode(FlushModeType type) {
        return withDec4(p -> p.query().setFlushMode(type));
    }
}
