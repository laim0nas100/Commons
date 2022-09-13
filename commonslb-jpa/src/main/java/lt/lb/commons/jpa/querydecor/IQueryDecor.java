package lt.lb.commons.jpa.querydecor;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;
import javax.persistence.EntityManager;
import javax.persistence.FlushModeType;
import javax.persistence.LockModeType;
import javax.persistence.NonUniqueResultException;
import javax.persistence.PersistenceException;
import javax.persistence.Query;
import javax.persistence.Tuple;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.AbstractQuery;
import javax.persistence.criteria.CompoundSelection;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.CriteriaUpdate;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Selection;
import javax.persistence.criteria.Subquery;
import javax.persistence.metamodel.MapAttribute;
import javax.persistence.metamodel.PluralAttribute;
import javax.persistence.metamodel.SingularAttribute;
import lt.lb.commons.F;
import lt.lb.commons.Nulls;
import lt.lb.commons.jpa.querydecor.DecoratorPhases.DecoratedQueryWithFinalPhase;
import lt.lb.commons.jpa.querydecor.DecoratorPhases.Phase2;
import lt.lb.commons.jpa.querydecor.DecoratorPhases.Phase3Common;
import lt.lb.commons.jpa.querydecor.DecoratorPhases.Phase3Query;
import lt.lb.commons.jpa.querydecor.DecoratorPhases.Phase3Subquery;
import lt.lb.commons.jpa.querydecor.DecoratorPhases.WithContext;
import lt.lb.commons.jpa.tuple.TupleProjection;
import lt.lb.uncheckedutils.SafeOpt;
import lt.lb.uncheckedutils.func.UncheckedFunction;

/**
 * @param <T_ROOT> Root Class type
 * @param <T_RESULT> Result Class type
 * @param <CTX> Mutable context type
 * @param <M> implementation
 * @author laim0nas100
 */
public interface IQueryDecor<T_ROOT, T_RESULT, CTX, M extends IQueryDecor<T_ROOT, T_RESULT, CTX, M>> extends WithContext<CTX> {

    public abstract <NEW_ROOT extends T_ROOT> IQueryDecor<NEW_ROOT, T_RESULT, CTX, ?> usingSubtype(Class<NEW_ROOT> subtype);

    public Class<T_ROOT> getRootClass();

    public Class<T_RESULT> getResultClass();

    public default CTX getContext() {
        return ctx();
    }

    public M withPred(boolean having, Function<DecoratorPhases.Phase2<T_ROOT, CTX>, Predicate> func);

    public M withPredCommon(Function<DecoratorPhases.Phase3Common<T_ROOT, CTX>, Predicate> func);

    public M withPredAny(boolean having, Function<DecoratorPhases.Phase3Abstract<T_ROOT, T_RESULT, CTX>, Predicate> func);

    public M withPredQuery(boolean having, Function<DecoratorPhases.Phase3Query<T_ROOT, T_RESULT, CTX>, Predicate> func);

    public M withPredSubquery(boolean having, Function<DecoratorPhases.Phase3Subquery<?, T_ROOT, T_RESULT, CTX>, Predicate> func);

    public M withDec1(Consumer<DecoratorPhases.Phase1<CTX>> cons);

    public M withDec2(Consumer<DecoratorPhases.Phase2<T_ROOT, CTX>> cons);

    public M withDec3Common(Consumer<DecoratorPhases.Phase3Common<T_ROOT, CTX>> cons);

    public M withDec3Any(Consumer<DecoratorPhases.Phase3Abstract<T_ROOT, T_RESULT, CTX>> cons);

    public M withDec3Query(Consumer<DecoratorPhases.Phase3Query<T_ROOT, T_RESULT, CTX>> cons);

    public M withDec3Subquery(Consumer<DecoratorPhases.Phase3Subquery<?, T_ROOT, T_RESULT, CTX>> cons);

    public M withDec4(Consumer<DecoratorPhases.Phase4<CTX>> cons);

    public M withResultModification(Function<List<T_RESULT>, List<T_RESULT>> func);

    public <RES> IQueryDecor<T_ROOT, RES, CTX, ?> selecting(
            Class<RES> resClass, Function<DecoratorPhases.Phase3Abstract<T_ROOT, RES, CTX>, Selection<RES>> func);

    public IQueryDecor<T_ROOT, Tuple, CTX, ?> selectingTuple(Function<DecoratorPhases.Phase3Abstract<T_ROOT, Tuple, CTX>, CompoundSelection<Tuple>> selection);

    public <RES> IQueryDecor<T_ROOT, RES, CTX, ?> selectingProjection(Class<RES> projection, Function<DecoratorPhases.Phase3Abstract<T_ROOT, RES, CTX>, CompoundSelection<RES>> selection);

    public <RES extends TupleProjection<T_ROOT>> IQueryDecor<T_ROOT, RES, CTX, ?> selectingTupleProjection(RES projection);

    public TypedQuery<T_RESULT> build(EntityManager em);

    public JpaQueryResultProvider<T_RESULT> buildResult(EntityManager em);

    public Query buildDeleteOrUpdate(EntityManager em, boolean delete);

    public DecoratedQueryWithFinalPhase<Phase3Query<T_ROOT, T_RESULT, CTX>, CriteriaQuery<T_RESULT>> decorateQueryRaw(EntityManager em);

    public default CriteriaQuery<T_RESULT> decorateQuery(EntityManager em) {
        return decorateQueryRaw(em).query;
    }

    public DecoratedQueryWithFinalPhase<Phase3Common<T_ROOT, CTX>, CriteriaDelete<T_ROOT>> decorateDeleteQuerRaw(EntityManager em);

    public default CriteriaDelete<T_ROOT> decorateDeleteQuery(EntityManager em) {
        return decorateDeleteQuerRaw(em).query;
    }

    public DecoratedQueryWithFinalPhase<Phase3Common<T_ROOT, CTX>, CriteriaUpdate<T_ROOT>> decorateUpdateQueryRaw(EntityManager em);

    public default CriteriaUpdate<T_ROOT> decorateUpdateQuery(EntityManager em) {
        return decorateUpdateQueryRaw(em).query;
    }

    public <PARENT_ROOT> DecoratedQueryWithFinalPhase<Phase3Subquery<PARENT_ROOT, T_ROOT, T_RESULT, CTX>, Subquery<T_RESULT>> decorateSubqueryRaw(EntityManager em, AbstractQuery<?> parentQuery, Root<PARENT_ROOT> parentRoot);

    public default <PARENT_ROOT> Subquery<T_RESULT> decorateSubquery(EntityManager em, AbstractQuery<?> parentQuery, Root<PARENT_ROOT> parentRoot) {
        return decorateSubqueryRaw(em, parentQuery, parentRoot).query;
    }

    public default M withPred(Function<DecoratorPhases.Phase2<T_ROOT, CTX>, Predicate> func) {
        return withPred(false, func);
    }

    public default M withPredAny(Function<DecoratorPhases.Phase3Abstract<T_ROOT, T_RESULT, CTX>, Predicate> func) {
        return withPredAny(false, func);
    }

    public default M withPredQuery(Function<DecoratorPhases.Phase3Query<T_ROOT, T_RESULT, CTX>, Predicate> func) {
        return withPredQuery(false, func);
    }

    public default M withPredSubquery(Function<DecoratorPhases.Phase3Subquery<?, T_ROOT, T_RESULT, CTX>, Predicate> func) {
        return withPredSubquery(false, func);
    }

    public default IQueryDecor<T_ROOT, T_ROOT, CTX, ?> selectingRoot() {
        return selecting(getRootClass(), p -> p.root());
    }

    public default <RES> IQueryDecor<T_ROOT, RES, CTX, ?> selecting(
            SingularAttribute<? super T_ROOT, RES> att) {
        Objects.requireNonNull(att);
        Class<RES> res = att.getJavaType();
        return selecting(res, p -> p.root().get(att));
    }

    public default IQueryDecor<T_ROOT, Tuple, CTX, ?> selectingTuple(SingularAttribute<? super T_ROOT, ?>... selections) {
        Nulls.requireNonNulls((Object[]) selections);
        return selectingTuple(p -> {
            Root<T_ROOT> root = p.root();
            Selection[] selArray = new Selection[selections.length];
            for (int i = 0; i < selections.length; i++) {
                selArray[i] = root.get(selections[i]);
            }
            return p.cb().tuple(selArray);
        });
    }

    public default IQueryDecor<T_ROOT, Tuple, CTX, ?> selectingTuple(Function<Phase2<? super T_ROOT, CTX>, Selection<?>>... selections) {
        Nulls.requireNonNulls((Object[]) selections);
        return selectingTuple(p -> {
            Selection[] selArray = new Selection[selections.length];
            for (int i = 0; i < selections.length; i++) {
                selArray[i] = selections[i].apply(p);
            }
            return p.cb().tuple(selArray);
        });
    }

    public default <RES> IQueryDecor<T_ROOT, RES, CTX, ?> selectingProjection(Class<RES> projection, SingularAttribute<? super T_ROOT, ?>... selections) {
        Nulls.requireNonNulls((Object[]) selections);
        return selectingProjection(projection, p -> {
            Root<T_ROOT> root = p.root();
            Selection[] selArray = new Selection[selections.length];
            for (int i = 0; i < selections.length; i++) {
                selArray[i] = root.get(selections[i]);
            }
            return p.cb().construct(projection, selArray);
        });
    }

    public default IQueryDecor<T_ROOT, Long, CTX, ?> selectingCount() {
        return selecting(Long.class, c -> c.cb().count(c.root()));
    }

    public default <RES> IQueryDecor<T_ROOT, Long, CTX, ?> selectingCount(SingularAttribute<? super T_ROOT, RES> att) {
        Objects.requireNonNull(att);
        return selecting(Long.class, c -> c.cb().count(c.root().get(att)));
    }

    public default IQueryDecor<T_ROOT, Long, CTX, ?> selectingCountDistinct() {
        return selecting(Long.class, c -> c.cb().countDistinct(c.root()));
    }

    public default <RES> IQueryDecor<T_ROOT, Long, CTX, ?> selectingCountDistinct(SingularAttribute<? super T_ROOT, RES> att) {
        Objects.requireNonNull(att);
        return selecting(Long.class, c -> c.cb().countDistinct(c.root().get(att)));
    }

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

    public default <ROOT, RES> M withSubqueryPred(IQueryDecor<ROOT, RES, CTX, ?> decor, Function<DecoratorPhases.Phase3Subquery<T_ROOT, ROOT, RES, CTX>, Predicate> func) {
        Objects.requireNonNull(decor);
        Objects.requireNonNull(func);

        return withPredAny(t -> {
            DecoratedQueryWithFinalPhase<Phase3Subquery<T_ROOT, ROOT, RES, CTX>, Subquery<RES>> raw = decor.decorateSubqueryRaw(t.em(), t.query(), t.root());
            return func.apply(DecoratorPhases.of(t.em(), t.cb(), t.root(), raw.phase.root(), raw.query, getContext()));
        });
    }

    public default <ROOT, RES> M withSubquery(IQueryDecor<ROOT, RES, CTX, ?> decor, Consumer<DecoratorPhases.Phase3Subquery<T_ROOT, ROOT, RES, CTX>> cons) {
        Objects.requireNonNull(decor);
        Objects.requireNonNull(cons);
        return withDec3Any(t -> {
            DecoratedQueryWithFinalPhase<Phase3Subquery<T_ROOT, ROOT, RES, CTX>, Subquery<RES>> raw = decor.decorateSubqueryRaw(t.em(), t.query(), t.root());
            cons.accept(DecoratorPhases.of(t.em(), t.cb(), t.root(), raw.phase.root(), raw.query, getContext()));
        });
    }

    public default M setDistinct(final boolean distinct) {
        return withDec3Any(c -> c.query().distinct(distinct));
    }

    public default M setOrderBy(boolean asc, JpaExpResolve<? super T_ROOT, ?, ? extends Path<T_ROOT>, ?, CTX>... att) {
        Nulls.requireNonNulls((Object[]) att);
        return withDec3Query(c -> {
            CriteriaBuilder cb = c.cb();
            Order[] order = Stream.of(att)
                    .map(at -> at.apply(F.cast(c)))
                    .map(m -> asc ? cb.asc(m) : cb.desc(m))
                    .toArray(s -> new Order[s]);
            c.query().orderBy(order);
        });
    }

    public default M setOrderBy(boolean asc, SingularAttribute<? super T_ROOT, ?>... att) {
        Nulls.requireNonNulls((Object[]) att);
        return withDec3Query(c -> {
            CriteriaBuilder cb = c.cb();
            Root<T_ROOT> root = c.root();
            Order[] order = Stream.of(att)
                    .map(at -> root.get(at))
                    .map(m -> asc ? cb.asc(m) : cb.desc(m))
                    .toArray(s -> new Order[s]);
            c.query().orderBy(order);
        });
    }

    public default M setOrderByAsc(SingularAttribute<? super T_ROOT, ?>... att) {
        return setOrderBy(true, att);
    }

    public default M setOrderByDesc(SingularAttribute<? super T_ROOT, ?>... att) {
        return setOrderBy(false, att);
    }

    public default M setOrderBy(Function<DecoratorPhases.Phase3Query<T_ROOT, T_RESULT, CTX>, List<Order>> func) {
        Objects.requireNonNull(func);
        return withDec3Query(c -> c.query().orderBy(func.apply(c)));
    }

    public default M setGroupBy(Function<DecoratorPhases.Phase3Abstract<T_ROOT, T_RESULT, CTX>, List<Expression<?>>> func) {
        Objects.requireNonNull(func);
        return withDec3Any(c -> c.query().groupBy(func.apply(c)));
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

    public default M setHint(String hint, Object value) {
        return withDec4(p -> p.query().setHint(hint, value));
    }

    public default M setFlushMode(FlushModeType type) {
        return withDec4(p -> p.query().setFlushMode(type));
    }

    public default SafeOpt<T_RESULT> buildUniqueResult(EntityManager em) {
        return SafeOpt.of(em).map(m -> {
            JpaQueryResultProvider<T_RESULT> provider = buildResult(em);
            List<T_RESULT> result = provider.getResultList();
            if (result == null || result.isEmpty()) {
                return null;
            }
            if (result.size() != 1) {
                throw new NonUniqueResultException(String.format("could not fetch unique result from query: %1s", provider.originalQuery()));
            }
            return result.get(0);
        });
    }

    public default T_RESULT buildUncheckedResult(EntityManager em) {
        return buildUniqueResult(em)
                .throwIfErrorUnwrapping(PersistenceException.class)
                .throwIfErrorRuntime()
                .throwNestedOrNull();
    }

    public default Stream<T_RESULT> buildStream(EntityManager em) {
        return buildResult(em).getResultStream();
    }

    public default List<T_RESULT> buildList(EntityManager em) {
        return buildResult(em).getResultList();
    }

    public default <U> U buildResult(EntityManager em, Function<JpaQueryResultProvider<T_RESULT>, U> mapper) {
        return Objects.requireNonNull(mapper, "Mapper is null").apply(buildResult(em));
    }

    public default <U> SafeOpt<U> buildResultSafe(EntityManager em, UncheckedFunction<JpaQueryResultProvider<T_RESULT>, U> mapper) {
        Objects.requireNonNull(mapper, "Mapper is null");
        return SafeOpt.of(em).map(m -> buildResult(m)).map(mapper);
    }

    public default int executeDeleteOrUpdate(EntityManager em, boolean delete) {
        return buildDeleteOrUpdate(em, delete).executeUpdate();
    }

    public default Query buildDelete(EntityManager em) {
        return buildDeleteOrUpdate(em, true);
    }

    public default Query buildUpdate(EntityManager em) {
        return buildDeleteOrUpdate(em, false);
    }

    public default int executeDelete(EntityManager em) {
        return buildDeleteOrUpdate(em, true).executeUpdate();
    }

    public default int executeUpdate(EntityManager em, boolean delete) {
        return buildDeleteOrUpdate(em, false).executeUpdate();
    }
}
