package lt.lb.commons.jpa.querydecor;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
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
import javax.persistence.Tuple;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.AbstractQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Selection;
import javax.persistence.criteria.Subquery;
import javax.persistence.metamodel.MapAttribute;
import javax.persistence.metamodel.PluralAttribute;
import javax.persistence.metamodel.SingularAttribute;
import lt.lb.commons.containers.collections.CollectionOp;
import lt.lb.commons.func.Lambda;
import lt.lb.commons.jpa.querydecor.DecoratorPhases.Phase1;
import lt.lb.commons.jpa.querydecor.DecoratorPhases.Phase2;
import lt.lb.commons.jpa.querydecor.DecoratorPhases.Phase3Query;
import lt.lb.commons.jpa.querydecor.DecoratorPhases.Phase3Subquery;
import lt.lb.commons.jpa.querydecor.DecoratorPhases.Phase4;
import lt.lb.uncheckedutils.SafeOpt;

/**
 *
 * @param <T_ROOT> Root Class type
 * @param <T_RESULT> Result Class type
 * @param <M> implementation
 *
 * @author laim0nas100
 */
public abstract class BaseJpaQueryDecor<T_ROOT, T_RESULT, M extends BaseJpaQueryDecor<T_ROOT, T_RESULT, M>> {

    protected ArrayDeque<Function<Phase3Query<T_ROOT, T_RESULT>, Predicate>> pred3 = null;
    protected ArrayDeque<Function<Phase3Subquery<T_ROOT, T_RESULT>, Predicate>> pred3Subquery = null;
    protected ArrayDeque<Function<Phase2<T_ROOT>, Predicate>> pred2 = null;

    protected ArrayDeque<Consumer<Phase1>> dec1 = null;
    protected ArrayDeque<Consumer<Phase2<T_ROOT>>> dec2 = null;
    /**
     * only when decorator is used to create/decorate a query
     */
    protected ArrayDeque<Consumer<Phase3Query<T_ROOT, T_RESULT>>> dec3 = null;
    /**
     * only when decorator is used to decorate a subquery
     */
    protected ArrayDeque<Consumer<Phase3Subquery<T_ROOT, T_RESULT>>> dec3Subquery = null;
    protected ArrayDeque<Consumer<Phase4<T_RESULT>>> dec4 = null;

    protected Function<Phase2<T_ROOT>, Expression<T_RESULT>> selection = null;
    protected Function<Phase2<T_ROOT>, List<Selection<?>>> multiselection = null;

    protected Class<T_ROOT> rootClass;
    protected Class<T_RESULT> resultClass;

    protected BaseJpaQueryDecor(BaseJpaQueryDecor copy) {
        if (copy != null) {
            pred2 = lazyInit(copy.pred2);
            pred3 = lazyInit(copy.pred3);
            pred3Subquery = lazyInit(copy.pred3Subquery);
            dec1 = lazyInit(copy.dec1);
            dec2 = lazyInit(copy.dec2);
            dec3 = lazyInit(copy.dec3);
            dec3Subquery = lazyInit(copy.dec3Subquery);
            dec4 = lazyInit(copy.dec4);

            selection = copy.selection;
            multiselection = copy.multiselection;
        }
    }

    protected abstract M me();

    protected boolean needQ1() {
        return this.dec1 != null;
    }

    protected boolean needQ2() {
        return this.dec2 != null || this.pred2 != null;
    }

    protected boolean needQ3() {
        return this.dec3 != null || this.pred3 != null;
    }

    protected int needPredCount(boolean sub) {
        int p2 = pred2 == null ? 0 : pred2.size();
        int p3 = sub ? (pred3 == null ? 0 : pred3.size()) : (pred3Subquery == null ? 0 : pred3Subquery.size());
        return p2 + p3;
    }

    protected boolean needQ3Sub() {
        return this.dec3Subquery != null || this.pred3Subquery != null;
    }

    protected boolean needQ4() {
        return this.dec4 != null;
    }

    public abstract <NEW_ROOT extends T_ROOT> BaseJpaQueryDecor<NEW_ROOT, T_RESULT, ?> usingSubtype(Class<NEW_ROOT> subtype);

    public BaseJpaQueryDecor<T_ROOT, T_ROOT, ?> selectingRoot() {
        return selecting(rootClass, p -> p.root());
    }

    public abstract <RES> BaseJpaQueryDecor<T_ROOT, RES, ?> selecting(
            Class<RES> resClass, Function<Phase2<T_ROOT>, Expression<RES>> func);

    public <RES> BaseJpaQueryDecor<T_ROOT, RES, ?> selecting(
            SingularAttribute<? super T_ROOT, RES> att) {
        Objects.requireNonNull(att);
        Class<RES> res = att.getJavaType();
        return selecting(res, p -> p.root().get(att));
    }

    public abstract BaseJpaQueryDecor<T_ROOT, Tuple, ?> selectingTuple(Function<Phase2<T_ROOT>, List<Selection<?>>> selections);

    public BaseJpaQueryDecor<T_ROOT, Tuple, ?> selectingTuple(List<Selection<?>> selections) {
        return selectingTuple(p -> selections);
    }

    public BaseJpaQueryDecor<T_ROOT, Tuple, ?> selectingTuple(Selection<?>... selections) {
        return selectingTuple(Arrays.asList(selections));
    }

    public BaseJpaQueryDecor<T_ROOT, Tuple, ?> selectingTuple(SingularAttribute<? super T_ROOT, ?>... selections) {
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

    public BaseJpaQueryDecor<T_ROOT, Long, ?> selectingCount() {
        return selecting(Long.class, c -> c.cb().count(c.root()));
    }

    public <RES> BaseJpaQueryDecor<T_ROOT, Long, ?> selectingCount(SingularAttribute<? super T_ROOT, RES> att) {
        Objects.requireNonNull(att);
        return selecting(Long.class, c -> c.cb().count(c.root().get(att)));
    }

    public BaseJpaQueryDecor<T_ROOT, Long, ?> selectingCountDistinct() {
        return selecting(Long.class, c -> c.cb().countDistinct(c.root()));
    }

    public <RES> BaseJpaQueryDecor<T_ROOT, Long, ?> selectingCountDistinct(SingularAttribute<? super T_ROOT, RES> att) {
        Objects.requireNonNull(att);
        return selecting(Long.class, c -> c.cb().countDistinct(c.root().get(att)));
    }

    public static <T> ArrayDeque<T> lazyAdd(ArrayDeque<T> current, T... items) {
        return CollectionOp.lazyAdd(ArrayDeque::new, current, items);
    }

    public static <T> ArrayDeque<T> lazyInit(ArrayDeque<T> items) {
        if (items == null || items.isEmpty()) {
            return null;
        }
        return new ArrayDeque<>(items);
    }

    public static <T> void lazyConsumers(ArrayDeque<Consumer<T>> consumers, T item) {
        if (consumers == null || consumers.isEmpty()) {
            return;
        }
        for (Consumer<T> cons : consumers) {
            SafeOpt.ofNullable(cons).ifPresent(func -> func.accept(item)).throwIfErrorAsNested();
        }
    }

    public static <T> void lazyPredicates(ArrayDeque<Function<T, Predicate>> predMakers, T item, List<Predicate> collector) {
        if (predMakers == null || predMakers.isEmpty()) {
            return;
        }
        for (Function<T, Predicate> maker : predMakers) {
            SafeOpt.ofNullable(maker).map(func -> func.apply(item))
                    .ifPresent(collector::add).throwIfErrorAsNested();
        }
    }

    public <T> M withPred(SingularAttribute<? super T_ROOT, T> att, BiFunction<CriteriaBuilder, Expression<T>, Predicate> func) {
        Objects.requireNonNull(att);
        Objects.requireNonNull(func);

        Function<Phase2<T_ROOT>, Predicate> fun = (Phase2<T_ROOT> t) -> func.apply(t.cb(), t.root().get(att));
        M of = me();
        of.pred2 = lazyAdd(of.pred2, fun);
        return of;
    }

    public <T, C extends Collection<T>> M withPred(PluralAttribute<T_ROOT, C, T> att, BiFunction<CriteriaBuilder, Expression<C>, Predicate> func) {
        Objects.requireNonNull(att);
        Objects.requireNonNull(func);
        Function<Phase2<T_ROOT>, Predicate> fun = (Phase2<T_ROOT> t) -> func.apply(t.cb(), t.root().get(att));
        M of = me();
        of.pred2 = lazyAdd(of.pred2, fun);
        return of;
    }

    public <K, V, MAP extends Map<K, V>> M withPred(MapAttribute<T_ROOT, K, V> att, BiFunction<CriteriaBuilder, Expression<MAP>, Predicate> func) {
        Objects.requireNonNull(att);
        Objects.requireNonNull(func);
        Function<Phase2<T_ROOT>, Predicate> fun = (Phase2<T_ROOT> t) -> func.apply(t.cb(), t.root().get(att));
        M of = me();
        of.pred2 = lazyAdd(of.pred2, fun);
        return of;
    }

    public M withPred(Function<Phase2<T_ROOT>, Predicate> func) {
        Objects.requireNonNull(func);
        M of = me();
        of.pred2 = lazyAdd(of.pred2, func);
        return of;
    }

    public <T> M withPredFunc(Function<Root<T_ROOT>, Expression<T>> att, Function<CriteriaBuilder, Function<Expression<T>, Predicate>> func) {
        return withPred(QueryDecor.of(att, func));
    }

    public <T> M withPredFunc(Function<Root<T_ROOT>, Expression<T>> att, T val, Function<CriteriaBuilder, BiFunction<Expression<T>, T, Predicate>> func) {
        return withPred(QueryDecor.of(att, val, func));
    }

    public <T> M withPredFunc(Function<Root<T_ROOT>, Expression<T>> att, T val, T val2, Function<CriteriaBuilder, Lambda.L3R<Expression<T>, T, T, Predicate>> func) {
        return withPred(QueryDecor.of(att, val, val2, func));
    }

    public <RES> M withSubqueryPred(BaseJpaQueryDecor<T_ROOT, RES, ?> decor, Function<Subquery<RES>, Predicate> func) {
        Objects.requireNonNull(decor);
        Objects.requireNonNull(func);
        Function<Phase3Query<T_ROOT, T_RESULT>, Predicate> fun = (Phase3Query<T_ROOT, T_RESULT> t) -> {
            Subquery<RES> subquery = t.query().subquery(decor.resultClass);
            subquery = decor.decorateSubquery(t.em(), subquery);
            return func.apply(subquery);
        };
        M of = me();
        of.pred3 = lazyAdd(of.pred3, fun);
        return of;
    }

    public <RES> M withSubquery(BaseJpaQueryDecor<T_ROOT, RES, ?> decor, Function<Phase3Subquery<T_ROOT, RES>, Predicate> func) {
        Objects.requireNonNull(decor);
        Objects.requireNonNull(func);
        Function<Phase3Query<T_ROOT, T_RESULT>, Predicate> fun = (Phase3Query<T_ROOT, T_RESULT> t) -> {
            CriteriaQuery<T_RESULT> query = t.query();
            Subquery<RES> subquery = query.subquery(decor.resultClass);
            subquery = decor.decorateSubquery(t.em(), subquery);
            Phase3Subquery<T_ROOT, RES> p3 = DecoratorPhases.of(t.em(), t.cb(), t.root(), subquery, query);
            return func.apply(p3);
        };
        M of = me();
        of.pred3 = lazyAdd(of.pred3, fun);
        return of;
    }

    public <RES> M withSubquery(BaseJpaQueryDecor<T_ROOT, RES, ?> decor, Consumer<Phase3Subquery<T_ROOT, RES>> cons) {
        Objects.requireNonNull(decor);
        Objects.requireNonNull(cons);
        Consumer<Phase3Query<T_ROOT, T_RESULT>> fun = (Phase3Query<T_ROOT, T_RESULT> t) -> {
            CriteriaQuery<T_RESULT> query = t.query();
            Subquery<RES> subquery = query.subquery(decor.resultClass);
            subquery = decor.decorateSubquery(t.em(), subquery);
            Phase3Subquery<T_ROOT, RES> p3 = DecoratorPhases.of(t.em(), t.cb(), t.root(), subquery, query);
            cons.accept(p3);
        };
        M of = me();
        of.dec3 = lazyAdd(of.dec3, fun);
        return of;
    }

    public M withPredQuery(Function<Phase3Query<T_ROOT, T_RESULT>, Predicate> func) {
        Objects.requireNonNull(func);
        M of = me();
        of.pred3 = lazyAdd(of.pred3, func);
        return of;
    }

    public M withPredSubquery(Function<Phase3Subquery<T_ROOT, T_RESULT>, Predicate> func) {
        Objects.requireNonNull(func);
        M of = me();
        of.pred3Subquery = lazyAdd(of.pred3Subquery, func);
        return of;
    }

    public M withDec1(Consumer<Phase1> cons) {
        Objects.requireNonNull(cons);
        M of = me();
        of.dec1 = lazyAdd(of.dec1, cons);
        return of;
    }

    public M withDec2(Consumer<Phase2<T_ROOT>> cons) {
        Objects.requireNonNull(cons);
        M of = me();
        of.dec2 = lazyAdd(of.dec2, cons);
        return of;
    }

    public M withDec3(Consumer<Phase3Query<T_ROOT, T_RESULT>> cons) {
        Objects.requireNonNull(cons);
        M of = me();
        of.dec3 = lazyAdd(of.dec3, cons);
        return of;
    }

    public M setDistinct(final boolean distinct) {
        return withDec3(c -> c.query().distinct(distinct));
    }

    public M setOrderByAsc(SingularAttribute<? super T_ROOT, ?>... att) {
        return withDec3(c -> {
            CriteriaBuilder cb = c.cb();
            Root<T_ROOT> root = c.root();
            Order[] order = Stream.of(att)
                    .map(at -> root.get(at))
                    .map(m -> cb.asc(m))
                    .toArray(s -> new Order[s]);
            c.query().orderBy(order);
        });
    }

    public M setOrderByDesc(SingularAttribute<? super T_ROOT, ?>... att) {
        return withDec3(c -> {
            CriteriaBuilder cb = c.cb();
            Root<T_ROOT> root = c.root();
            Order[] order = Stream.of(att)
                    .map(at -> root.get(at))
                    .map(m -> cb.desc(m))
                    .toArray(s -> new Order[s]);
            c.query().orderBy(order);
        });
    }

    public M setOrderBy(Function<Phase3Query<T_ROOT, T_RESULT>, List<Order>> func) {
        return withDec3(c -> c.query().orderBy(func.apply(c)));
    }

    public M setOrderBy(Order... order) {
        return withDec3(c -> c.query().orderBy(order));
    }

    public M setOrderBy(List<Order> order) {
        return withDec3(c -> c.query().orderBy(order));
    }

    public M setMaxResults(int max) {
        return withDec4(p -> p.typedQuery().setMaxResults(max));
    }

    public M setFirstResult(int startPosition) {
        return withDec4(p -> p.typedQuery().setFirstResult(startPosition));
    }

    public M setLockMode(LockModeType lock) {
        return withDec4(p -> p.typedQuery().setLockMode(lock));
    }

    public M setFlushMode(FlushModeType type) {
        return withDec4(p -> p.typedQuery().setFlushMode(type));
    }

    public M withDec3Subquery(Consumer<Phase3Subquery<T_ROOT, T_RESULT>> cons) {
        Objects.requireNonNull(cons);
        M of = me();
        of.dec3Subquery = lazyAdd(of.dec3Subquery, cons);
        return of;
    }

    public M withDec4(Consumer<Phase4<T_RESULT>> cons) {
        Objects.requireNonNull(cons);
        M of = me();
        of.dec4 = lazyAdd(of.dec4, cons);
        return of;
    }

    public SafeOpt<T_RESULT> buildUniqueResult(EntityManager em) {
        return SafeOpt.ofGet(() -> {
            TypedQuery<T_RESULT> query = build(em);
            List<T_RESULT> result = query.getResultList();
            if (result != null && !result.isEmpty()) {
                if (result.size() != 1) {
                    throw new NonUniqueResultException(String.format("could not fetch unique result from query: %1s", query));
                }
                return result.get(0);
            } else {
                return null;
            }
        });
    }

    public T_RESULT buildUncheckedResult(EntityManager em) {
        return buildUniqueResult(em)
                .throwIfErrorUnwrapping(PersistenceException.class)
                .throwIfErrorRuntime()
                .throwNestedOrNull();
    }

    public Stream<T_RESULT> buildStream(EntityManager em) {
        TypedQuery<T_RESULT> tq = build(em);
        try {
            return tq.getResultStream();
        } catch (UnsupportedOperationException unsupported) {
            return tq.getResultList().stream();
        }
    }

    public Stream<T_RESULT> buildListStream(EntityManager em) {
        return build(em).getResultList().stream();
    }

    public List<T_RESULT> buildList(EntityManager em) {
        return build(em).getResultList();
    }

    public TypedQuery<T_RESULT> build(EntityManager em) {
        TypedQuery<T_RESULT> typed = em.createQuery(decorateQuery(em));

        if (needQ4()) {
            lazyConsumers(dec4, DecoratorPhases.of(typed));
        }

        return typed;
    }

    protected <PARENT_ROOT> void decorateQuery(Phase2<T_ROOT> p2, CriteriaQuery<T_RESULT> query, Subquery<T_RESULT> subquery, AbstractQuery<PARENT_ROOT> parentQuery) {
        if (query == null && subquery == null) {
            throw new IllegalArgumentException("Supply query or subquery");
        }
        if (query != null && subquery != null) {
            throw new IllegalArgumentException("Supply query or subquery");
        }
        boolean q2 = needQ2();
        boolean q3 = needQ3();
        boolean q3sub = needQ3Sub();
        if (q2 || q3 || q3sub) {
            ArrayList<Predicate> predicates = new ArrayList<>(needPredCount(false));

            if (q2) {
                lazyConsumers(dec2, p2);
                lazyPredicates(pred2, p2, predicates);
            }

            if (q3 && query != null) {
                Phase3Query<T_ROOT, T_RESULT> p3 = DecoratorPhases.of(p2, query);
                lazyConsumers(dec3, p3);
                lazyPredicates(pred3, p3, predicates);

            } else if (q3sub && subquery != null) {
                Phase3Subquery<T_ROOT, T_RESULT> p3 = DecoratorPhases.of(p2, subquery, parentQuery);
                lazyConsumers(dec3Subquery, p3);
                lazyPredicates(pred3Subquery, p3, predicates);

            }
            if (!predicates.isEmpty()) {
                Predicate[] toArray = predicates.toArray(new Predicate[predicates.size()]);
                if (query != null) {
                    query.where(toArray);
                } else if (subquery != null) {
                    subquery.where(toArray);
                }
            }

        }
    }

    public CriteriaQuery<T_RESULT> decorateQuery(EntityManager em) {
        Objects.requireNonNull(em);
        CriteriaBuilder builder = em.getCriteriaBuilder();

        if (needQ1()) {
            lazyConsumers(dec1, DecoratorPhases.of(em, builder));
        }

        CriteriaQuery<T_RESULT> query = builder.createQuery(resultClass);
        Root<T_ROOT> root = query.from(rootClass);
        Phase2<T_ROOT> p2 = DecoratorPhases.of(em, builder, root);
        decorateQuery(p2, query, null, null);

        if (Tuple.class.equals(resultClass)) {
            if (multiselection != null) {
                query.multiselect(multiselection.apply(p2));
            }
        } else {
            if (selection != null) {
                query.select(selection.apply(p2));
            }
        }

        return query;

    }

    public <PARENT_ROOT> Subquery<T_RESULT> decorateSubquery(EntityManager em, AbstractQuery<PARENT_ROOT> parentQuery) {
        Objects.requireNonNull(em);
        Objects.requireNonNull(parentQuery);

        CriteriaBuilder builder = em.getCriteriaBuilder();
        if (needQ1()) {
            lazyConsumers(dec1, DecoratorPhases.of(em, builder));
        }

        Subquery<T_RESULT> subquery = parentQuery.subquery(resultClass);
        Root<T_ROOT> root = subquery.from(rootClass);
        Phase2<T_ROOT> p2 = DecoratorPhases.of(em, builder, root);

        decorateQuery(p2, null, subquery, parentQuery);
        if (selection != null) {
            subquery.select(selection.apply(p2));
        }

        return subquery;

    }
}
