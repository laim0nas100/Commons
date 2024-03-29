package lt.lb.commons.jpa.querydecor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.Tuple;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.AbstractQuery;
import javax.persistence.criteria.CommonAbstractCriteria;
import javax.persistence.criteria.CompoundSelection;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.CriteriaUpdate;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Selection;
import javax.persistence.criteria.Subquery;
import javax.persistence.metamodel.SingularAttribute;
import lt.lb.commons.F;
import lt.lb.commons.jpa.querydecor.DecoratorPhases.DecoratedQueryWithFinalPhase;
import lt.lb.commons.jpa.querydecor.DecoratorPhases.Phase1;
import lt.lb.commons.jpa.querydecor.DecoratorPhases.Phase2;
import lt.lb.commons.jpa.querydecor.DecoratorPhases.Phase3Abstract;
import lt.lb.commons.jpa.querydecor.DecoratorPhases.Phase3Common;
import lt.lb.commons.jpa.querydecor.DecoratorPhases.Phase3Query;
import lt.lb.commons.jpa.querydecor.DecoratorPhases.Phase3Subquery;
import lt.lb.commons.jpa.querydecor.DecoratorPhases.Phase4;
import lt.lb.commons.jpa.querydecor.LazyUtil.LazyPredAdd;
import static lt.lb.commons.jpa.querydecor.LazyUtil.lazyAdd;
import static lt.lb.commons.jpa.querydecor.LazyUtil.lazyConsumers;
import static lt.lb.commons.jpa.querydecor.LazyUtil.lazyInit;
import static lt.lb.commons.jpa.querydecor.LazyUtil.lazyPredicates;
import lt.lb.commons.jpa.tuple.TupleProjection;

/**
 *
 * @param <T_ROOT> Root Class type
 * @param <T_RESULT> Result Class type
 * @param <CTX> Mutable context type
 * @param <M> implementation
 *
 * @author laim0nas100
 */
public abstract class BaseJpaQueryDecor<T_ROOT, T_RESULT, CTX, M extends BaseJpaQueryDecor<T_ROOT, T_RESULT, CTX, M>> implements IQueryDecor<T_ROOT, T_RESULT, CTX, M> {

    protected ArrayList<Function<Phase3Common<T_ROOT, CTX>, Predicate>> pred3Common = null;
    protected ArrayList<Function<Phase3Abstract<T_ROOT, T_RESULT, CTX>, Predicate>> pred3 = null;
    protected ArrayList<Function<Phase3Query<T_ROOT, T_RESULT, CTX>, Predicate>> pred3Query = null;
    protected ArrayList<Function<Phase3Subquery<?, T_ROOT, T_RESULT, CTX>, Predicate>> pred3Subquery = null;
    protected ArrayList<Function<Phase2<T_ROOT, CTX>, Predicate>> pred2 = null;

    protected ArrayList<Function<Phase3Abstract<T_ROOT, T_RESULT, CTX>, Predicate>> pred3Having = null;
    protected ArrayList<Function<Phase3Query<T_ROOT, T_RESULT, CTX>, Predicate>> pred3QueryHaving = null;
    protected ArrayList<Function<Phase3Subquery<?, T_ROOT, T_RESULT, CTX>, Predicate>> pred3SubqueryHaving = null;
    protected ArrayList<Function<Phase2<T_ROOT, CTX>, Predicate>> pred2Having = null;

    protected ArrayList<Consumer<Phase1<CTX>>> dec1 = null;
    protected ArrayList<Consumer<Phase2<T_ROOT, CTX>>> dec2 = null;
    protected ArrayList<Consumer<Phase3Common<T_ROOT, CTX>>> dec3Common = null;
    protected ArrayList<Consumer<Phase3Abstract<T_ROOT, T_RESULT, CTX>>> dec3 = null;
    /**
     * only when decorator is used to create/decorate a query
     */
    protected ArrayList<Consumer<Phase3Query<T_ROOT, T_RESULT, CTX>>> dec3Query = null;
    /**
     * only when decorator is used to decorate a subquery
     */
    protected ArrayList<Consumer<Phase3Subquery<?, T_ROOT, T_RESULT, CTX>>> dec3Subquery = null;
    protected ArrayList<Consumer<Phase4<CTX>>> dec4 = null;

    protected ArrayList<Function<List<T_RESULT>, List<T_RESULT>>> resultProviderModifiers = null;

    protected Function<Phase3Abstract<T_ROOT, T_RESULT, CTX>, ? extends Selection<T_RESULT>> selection = null;
    protected TupleProjection<T_ROOT> tupleProjection = null;

    protected Class<T_ROOT> rootClass;
    protected Class<T_RESULT> resultClass;
    protected CTX context;

    protected BaseJpaQueryDecor(BaseJpaQueryDecor copy) {
        if (copy != null) {
            pred2 = lazyInit(copy.pred2);
            pred3 = lazyInit(copy.pred3);
            pred3Common = lazyInit(copy.pred3Common);
            pred3Query = lazyInit(copy.pred3Query);
            pred3Subquery = lazyInit(copy.pred3Subquery);
            pred2Having = lazyInit(copy.pred2Having);
            pred3Having = lazyInit(copy.pred3Having);
            pred3QueryHaving = lazyInit(copy.pred3QueryHaving);
            pred3SubqueryHaving = lazyInit(copy.pred3SubqueryHaving);
            dec1 = lazyInit(copy.dec1);
            dec2 = lazyInit(copy.dec2);
            dec3 = lazyInit(copy.dec3);
            dec3Common = lazyInit(copy.dec3Common);
            dec3Query = lazyInit(copy.dec3Query);
            dec3Subquery = lazyInit(copy.dec3Subquery);
            dec4 = lazyInit(copy.dec4);
            resultProviderModifiers = lazyInit(copy.resultProviderModifiers);
            context = copyContext((CTX) copy.context);
            selection = copy.selection;
            tupleProjection = copy.tupleProjection;
        }
    }

    protected CTX copyContext(CTX ctx) {
        return ctx;
    }

    @Override
    public Class<T_ROOT> getRootClass() {
        return rootClass;
    }

    @Override
    public Class<T_RESULT> getResultClass() {
        return resultClass;
    }

    @Override
    public CTX ctx() {
        return context;
    }

    protected abstract M me();

    protected boolean needQ1() {
        return this.dec1 != null;
    }

    protected static boolean hasNonNull(Collection... objs) {
        for (Collection col : objs) {
            if (col != null) {
                if (!col.isEmpty()) {
                    return true;
                }
            }
        }
        return false;
    }

    protected static int countNonNull(Object... objs) {
        int count = 0;
        for (Object ob : objs) {
            if (ob != null) {
                count++;
            }
        }
        return count;
    }

    protected boolean needQ4() {
        return this.dec4 != null;
    }

    @Override
    public abstract <NEW_ROOT extends T_ROOT> BaseJpaQueryDecor<NEW_ROOT, T_RESULT, CTX, ?> usingSubtype(Class<NEW_ROOT> subtype);

    @Override
    public BaseJpaQueryDecor<T_ROOT, T_ROOT, CTX, ?> selectingRoot() {
        return selecting(rootClass, p -> p.root());
    }

    @Override
    public abstract <RES> BaseJpaQueryDecor<T_ROOT, RES, CTX, ?> selecting(
            Class<RES> resClass, Function<Phase3Abstract<T_ROOT, RES, CTX>, Selection<RES>> func);

    @Override
    public abstract <RES extends TupleProjection<T_ROOT>> BaseJpaQueryDecor<T_ROOT, RES, CTX, ?> selectingTupleProjection(RES projection);

    @Override
    public <RES> BaseJpaQueryDecor<T_ROOT, RES, CTX, ?> selecting(
            SingularAttribute<? super T_ROOT, RES> att) {
        Objects.requireNonNull(att);
        Class<RES> res = att.getJavaType();
        return selecting(res, p -> p.root().get(att));
    }

    @Override
    public BaseJpaQueryDecor<T_ROOT, Tuple, CTX, ?> selectingTuple(Function<Phase3Abstract<T_ROOT, Tuple, CTX>, CompoundSelection<Tuple>> selection) {
        Objects.requireNonNull(selection);
        return selecting(Tuple.class, f -> selection.apply(f));
    }

    @Override
    public <RES> BaseJpaQueryDecor<T_ROOT, RES, CTX, ?> selectingProjection(Class<RES> projection, Function<Phase3Abstract<T_ROOT, RES, CTX>, CompoundSelection<RES>> selection) {
        Objects.requireNonNull(selection);
        return selecting(projection, f -> selection.apply(f));
    }

    @Override
    public M withPredCommon(Function<Phase3Common<T_ROOT, CTX>, Predicate> func) {
        Objects.requireNonNull(func);
        M of = me();
        of.pred3Common = lazyAdd(of.pred3Common, func);
        return of;
    }

    @Override
    public M withPred(boolean having, Function<Phase2<T_ROOT, CTX>, Predicate> func) {
        Objects.requireNonNull(func);
        M of = me();
        if (having) {
            of.pred2Having = lazyAdd(of.pred2Having, func);
        } else {
            of.pred2 = lazyAdd(of.pred2, func);
        }

        return of;
    }

    @Override
    public M withPredAny(boolean having, Function<DecoratorPhases.Phase3Abstract<T_ROOT, T_RESULT, CTX>, Predicate> func) {
        Objects.requireNonNull(func);
        M of = me();
        if (having) {
            of.pred3Having = lazyAdd(of.pred3Having, func);
        } else {
            of.pred3 = lazyAdd(of.pred3, func);
        }
        return of;
    }

    @Override
    public M withPredQuery(boolean having, Function<Phase3Query<T_ROOT, T_RESULT, CTX>, Predicate> func) {
        Objects.requireNonNull(func);
        M of = me();
        if (having) {
            of.pred3QueryHaving = lazyAdd(of.pred3QueryHaving, func);
        } else {
            of.pred3Query = lazyAdd(of.pred3Query, func);
        }
        return of;
    }

    @Override
    public M withPredSubquery(boolean having, Function<Phase3Subquery<?, T_ROOT, T_RESULT, CTX>, Predicate> func) {
        Objects.requireNonNull(func);
        M of = me();
        if (having) {
            of.pred3SubqueryHaving = lazyAdd(of.pred3SubqueryHaving, func);
        } else {
            of.pred3Subquery = lazyAdd(of.pred3Subquery, func);
        }
        return of;
    }

    @Override
    public M withDec1(Consumer<Phase1<CTX>> cons) {
        Objects.requireNonNull(cons);
        M of = me();
        of.dec1 = lazyAdd(of.dec1, cons);
        return of;
    }

    @Override
    public M withDec2(Consumer<Phase2<T_ROOT, CTX>> cons) {
        Objects.requireNonNull(cons);
        M of = me();
        of.dec2 = lazyAdd(of.dec2, cons);
        return of;
    }

    @Override
    public M withDec3Common(Consumer<Phase3Common<T_ROOT, CTX>> cons) {
        Objects.requireNonNull(cons);
        M of = me();
        of.dec3Common = lazyAdd(of.dec3Common, cons);
        return of;
    }

    @Override
    public M withDec3Any(Consumer<Phase3Abstract<T_ROOT, T_RESULT, CTX>> cons) {
        Objects.requireNonNull(cons);
        M of = me();
        of.dec3 = lazyAdd(of.dec3, cons);
        return of;
    }

    @Override
    public M withDec3Query(Consumer<Phase3Query<T_ROOT, T_RESULT, CTX>> cons) {
        Objects.requireNonNull(cons);
        M of = me();
        of.dec3Query = lazyAdd(of.dec3Query, cons);
        return of;
    }

    @Override
    public M withDec3Subquery(Consumer<Phase3Subquery<?, T_ROOT, T_RESULT, CTX>> cons) {
        Objects.requireNonNull(cons);
        M of = me();
        of.dec3Subquery = lazyAdd(of.dec3Subquery, cons);
        return of;
    }

    @Override
    public M withDec4(Consumer<Phase4<CTX>> cons) {
        Objects.requireNonNull(cons);
        M of = me();
        of.dec4 = lazyAdd(of.dec4, cons);
        return of;
    }

    @Override
    public M withResultModification(Function<List<T_RESULT>, List<T_RESULT>> func) {
        Objects.requireNonNull(func);
        M of = me();
        of.resultProviderModifiers = lazyAdd(of.resultProviderModifiers, func);
        return of;
    }

    @Override
    public TypedQuery<T_RESULT> build(EntityManager em) {

        TypedQuery<T_RESULT> typed = em.createQuery(decorateQuery(em));

        if (needQ4()) {
            lazyConsumers(dec4, DecoratorPhases.of(em, typed, getContext()));
        }

        return typed;
    }

    @Override
    public JpaQueryResultProvider<T_RESULT> buildResult(EntityManager em) {
        JpaQueryResultProvider<T_RESULT> provider = JpaQueryResultProvider.of(build(em));

        if (resultProviderModifiers != null) {
            for (Function<List<T_RESULT>, List<T_RESULT>> func : resultProviderModifiers) {
                provider = provider.modified(func);
            }
        }
        return provider;
    }

    @Override
    public Query buildDeleteOrUpdate(EntityManager em, boolean delete) {
        Query query;
        if (delete) {
            query = em.createQuery(decorateDeleteQuery(em));
        } else {
            query = em.createQuery(decorateUpdateQuery(em));
        }
        if (needQ4()) {
            lazyConsumers(dec4, DecoratorPhases.of(em, query, getContext()));
        }

        return query;
    }

    /**
     * return last phase
     *
     * @param p2
     * @param commonCriteria
     * @return
     */
    protected Phase3Common<T_ROOT, CTX> decorateCommonAbstractCriteria(Phase2<T_ROOT, CTX> p2, CommonAbstractCriteria commonCriteria) {
        if (commonCriteria == null) {
            throw new IllegalArgumentException("Supply only commonAbstractCriteria");
        }

        Phase3Common<T_ROOT, CTX> p3Common = DecoratorPhases.of(p2, commonCriteria);
        LazyPredAdd predicates = new LazyPredAdd();
        lazyConsumers(dec2, p2);
        lazyPredicates(pred2, p2, predicates::add);
        lazyConsumers(dec3Common, p3Common);
        lazyPredicates(pred3Common, p3Common, predicates::add);

        if (predicates.hasItems()) {
            applyWhere(commonCriteria, predicates.toArray());
        }
        return p3Common;
    }

    /**
     * return last phase
     *
     * @param <PARENT_RESULT>
     * @param p2
     * @param query
     * @param subquery
     * @param parentQuery
     * @param parentRoot
     * @return
     */
    protected <PARENT_RESULT> Phase3Abstract<T_ROOT, T_RESULT, CTX> decorateQuery(Phase2<T_ROOT, CTX> p2, CriteriaQuery<T_RESULT> query, Subquery<T_RESULT> subquery, AbstractQuery<?> parentQuery, Root<PARENT_RESULT> parentRoot) {
        if (countNonNull(query, subquery) != 1) {
            throw new IllegalArgumentException("Supply only one of query or subquery");
        }

        if (query != null || subquery != null) {
            LazyPredAdd predicates = new LazyPredAdd();
            LazyPredAdd predicatesHaving = new LazyPredAdd();

            lazyConsumers(dec2, p2);
            lazyPredicates(pred2, p2, predicates::add);
            lazyPredicates(pred2Having, p2, predicatesHaving::add);
            Phase3Abstract<T_ROOT, T_RESULT, CTX> p3 = null;

            Phase3Query<T_ROOT, T_RESULT, CTX> p3Q = null;
            Phase3Subquery<?, T_ROOT, T_RESULT, CTX> p3Sub = null;

            if (query != null) {
                p3Q = DecoratorPhases.of(p2, query);
                p3 = p3Q;

            } else { // subquery must not be null
                p3Sub = DecoratorPhases.of(p2.em(), p2.cb(), parentRoot, p2.root(), subquery, parentQuery, p2.ctx());
                p3 = p3Sub;
            }
            lazyConsumers(dec3, p3);
            lazyPredicates(pred3, p3, predicates::add);
            lazyPredicates(pred3Having, p3, predicatesHaving::add);

            AbstractQuery<T_RESULT> aq;
            if (query != null) {
                aq = query;
                lazyConsumers(dec3Query, p3Q);
                lazyPredicates(pred3Query, p3Q, predicates::add);
                lazyPredicates(pred3QueryHaving, p3Q, predicatesHaving::add);
            } else {
                aq = subquery;
                lazyConsumers(dec3Subquery, p3Sub);
                lazyPredicates(pred3Subquery, p3Sub, predicates::add);
                lazyPredicates(pred3SubqueryHaving, p3Sub, predicatesHaving::add);
            }

            if (predicates.hasItems()) {
                applyWhere(aq, predicates.toArray());
            }

            if (predicatesHaving.hasItems()) {
                aq.having(predicatesHaving.toArray());
            }

            return p3;
        }
        return null;

    }

    protected void applyWhere(CommonAbstractCriteria commonCriteria, Predicate[] whereArray) {

        if (commonCriteria instanceof AbstractQuery) {
            AbstractQuery abstractQuery = (AbstractQuery) commonCriteria;
            abstractQuery.where(whereArray);
        } else if (commonCriteria instanceof CriteriaDelete) {
            CriteriaDelete delete = (CriteriaDelete) commonCriteria;
            delete.where(whereArray);
        } else if (commonCriteria instanceof CriteriaUpdate) {
            CriteriaUpdate update = (CriteriaUpdate) commonCriteria;
            update.where(whereArray);
        } else {
            throw new IllegalArgumentException("Urecognized query " + commonCriteria);
        }
    }

    @Override
    public DecoratedQueryWithFinalPhase<Phase3Common<T_ROOT, CTX>, CriteriaDelete<T_ROOT>> decorateDeleteQuerRaw(EntityManager em) {
        Objects.requireNonNull(em);
        CriteriaBuilder builder = em.getCriteriaBuilder();

        if (needQ1()) {
            lazyConsumers(dec1, DecoratorPhases.of(em, builder, getContext()));
        }

        CriteriaDelete<T_ROOT> query = builder.createCriteriaDelete(getRootClass());
        Root<T_ROOT> root = query.from(getRootClass());
        Phase2<T_ROOT, CTX> p2 = DecoratorPhases.of(em, builder, root, getContext());
        Phase3Common<T_ROOT, CTX> phase = decorateCommonAbstractCriteria(p2, query);
        return new DecoratedQueryWithFinalPhase<>(phase, query);
    }

    @Override
    public DecoratedQueryWithFinalPhase<Phase3Common<T_ROOT, CTX>, CriteriaUpdate<T_ROOT>> decorateUpdateQueryRaw(EntityManager em) {
        Objects.requireNonNull(em);
        CriteriaBuilder builder = em.getCriteriaBuilder();

        if (needQ1()) {
            lazyConsumers(dec1, DecoratorPhases.of(em, builder, getContext()));
        }

        CriteriaUpdate<T_ROOT> query = builder.createCriteriaUpdate(getRootClass());
        Root<T_ROOT> root = query.from(getRootClass());
        Phase2<T_ROOT, CTX> p2 = DecoratorPhases.of(em, builder, root, getContext());
        Phase3Common<T_ROOT, CTX> phase = decorateCommonAbstractCriteria(p2, query);
        return new DecoratedQueryWithFinalPhase<>(phase, query);
    }

    @Override
    public CriteriaUpdate<T_ROOT> decorateUpdateQuery(EntityManager em) {
        return decorateUpdateQueryRaw(em).query;
    }

    @Override
    public DecoratedQueryWithFinalPhase<Phase3Query<T_ROOT, T_RESULT, CTX>, CriteriaQuery<T_RESULT>> decorateQueryRaw(EntityManager em) {
        Objects.requireNonNull(em);
        CriteriaBuilder builder = em.getCriteriaBuilder();

        if (needQ1()) {
            lazyConsumers(dec1, DecoratorPhases.of(em, builder, getContext()));
        }

        CriteriaQuery<T_RESULT> query = builder.createQuery(getResultClass());
        Root<T_ROOT> root = query.from(getRootClass());
        Phase2<T_ROOT, CTX> p2 = DecoratorPhases.of(em, builder, root, getContext());
        Phase3Abstract<T_ROOT, T_RESULT, CTX> phase = decorateQuery(p2, query, null, null, null);

        if (selection != null) {
            query.select(selection.apply(phase));
        } else if (tupleProjection != null) {
            List<Selection<?>> selections = tupleProjection.getAllSelections(phase);
            if (selections.isEmpty()) {
                throw new IllegalStateException("No selections specified in tupleProjection");
            }
            query.multiselect(selections);

        } else {
            throw new IllegalStateException("No selection specified for query");
        }

        return new DecoratedQueryWithFinalPhase<>(F.cast(phase), query);

    }

    @Override
    public <PARENT_ROOT> DecoratedQueryWithFinalPhase<Phase3Subquery<PARENT_ROOT, T_ROOT, T_RESULT, CTX>, Subquery<T_RESULT>> decorateSubqueryRaw(EntityManager em, AbstractQuery<?> parentQuery, Root<PARENT_ROOT> parentRoot) {
        Objects.requireNonNull(em);
        Objects.requireNonNull(parentQuery);
        Objects.requireNonNull(parentRoot);

        CriteriaBuilder builder = em.getCriteriaBuilder();
        if (needQ1()) {
            lazyConsumers(dec1, DecoratorPhases.of(em, builder, getContext()));
        }

        Subquery<T_RESULT> subquery = parentQuery.subquery(getResultClass());
        Root<T_ROOT> root = subquery.from(getRootClass());
        Phase2<T_ROOT, CTX> p2 = DecoratorPhases.of(em, builder, root, getContext());

        Phase3Abstract<T_ROOT, T_RESULT, CTX> phase = decorateQuery(p2, null, subquery, parentQuery, parentRoot);
        if (selection != null) {
            Selection<T_RESULT> apply = selection.apply(phase);
            if (apply instanceof Expression) {
                subquery.select(F.cast(apply));
            } else {
                throw new IllegalArgumentException(apply + " must be an expression to be used as subquery");
            }

        } else {
            if (tupleProjection != null) {
                throw new IllegalStateException("tuple projection is not possible for subquery");
            }
            throw new IllegalStateException("No selection specified for subquery");
        }

        return new DecoratedQueryWithFinalPhase<>(F.cast(phase), subquery);
    }
}
