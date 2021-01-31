package lt.lb.commons.jpa.querydecor;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;
import javax.persistence.EntityManager;
import javax.persistence.NonUniqueResultException;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.AbstractQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;
import javax.persistence.metamodel.MapAttribute;
import javax.persistence.metamodel.PluralAttribute;
import javax.persistence.metamodel.SingularAttribute;
import lt.lb.commons.SafeOpt;
import lt.lb.commons.containers.collections.CollectionOp;
import lt.lb.commons.jpa.querydecor.DecoratorPhases.Phase1;
import lt.lb.commons.jpa.querydecor.DecoratorPhases.Phase2;
import lt.lb.commons.jpa.querydecor.DecoratorPhases.Phase3Query;
import lt.lb.commons.jpa.querydecor.DecoratorPhases.Phase3Subquery;
import lt.lb.commons.jpa.querydecor.DecoratorPhases.Phase4;

/**
 *
 * @param <T_ROOT> Root Class type
 * @param <T_RESULT> Result Class type
 * @param <M> implementation
 *
 * @author laim0nas100
 */
public abstract class BaseJpaQueryDecor<T_ROOT, T_RESULT, M extends BaseJpaQueryDecor<T_ROOT, T_RESULT, M>> {

    protected ArrayDeque<Function<Phase3Query<T_ROOT, T_RESULT>, Predicate>> pred3Query = null;
    protected ArrayDeque<Function<Phase3Subquery<T_ROOT, T_RESULT>, Predicate>> pred3Subquery = null;
    protected ArrayDeque<Function<Phase2<T_ROOT>, Predicate>> pred2 = null;

    protected ArrayDeque<Consumer<Phase1>> dec1 = null;
    protected ArrayDeque<Consumer<Phase2<T_ROOT>>> dec2 = null;
    /**
     * only when decorator is used to create/decorate a query
     */
    protected ArrayDeque<Consumer<Phase3Query<T_ROOT, T_RESULT>>> dec3Query = null;
    /**
     * only when decorator is used to decorate a subquery
     */
    protected ArrayDeque<Consumer<Phase3Subquery<T_ROOT, T_RESULT>>> dec3Subquery = null;
    protected ArrayDeque<Consumer<Phase4<T_RESULT>>> dec4 = null;

    protected Function<Phase2<T_ROOT>, Expression<T_RESULT>> selection;

    protected Class<T_ROOT> rootClass;
    protected Class<T_RESULT> resultClass;

    protected BaseJpaQueryDecor(BaseJpaQueryDecor copy) {
        if (copy != null) {
            pred2 = lazyInit(copy.pred2);
            pred3Query = lazyInit(copy.pred3Query);
            pred3Subquery = lazyInit(copy.pred3Subquery);
            dec1 = lazyInit(copy.dec1);
            dec2 = lazyInit(copy.dec2);
            dec3Query = lazyInit(copy.dec3Query);
            dec3Subquery = lazyInit(copy.dec3Subquery);
            dec4 = lazyInit(copy.dec4);

            selection = copy.selection;
        }
    }

    protected abstract M me();

    public abstract <RES> BaseJpaQueryDecor withResult(
            Class<RES> resClass, Function<Phase2<T_ROOT>, Expression<RES>> func);

    public <RES> BaseJpaQueryDecor withResult(
            SingularAttribute<T_ROOT, RES> att) {
        Objects.requireNonNull(att);
        Class<RES> res = att.getJavaType();
        return withResult(res, p -> p.root().get(att));
    }

    public static <T> ArrayDeque<T> lazyAdd(ArrayDeque<T> current, T... items) {
        return CollectionOp.lazyAdd(() -> new ArrayDeque<>(items.length), current, items);
    }

    public static <T> ArrayDeque<T> lazyInit(ArrayDeque<T> items) {
        if (items == null || items.isEmpty()) {
            return null;
        }
        return new ArrayDeque<>(items);
    }

    public static <T> ArrayDeque<T> lazyCopy(ArrayDeque<T> source, ArrayDeque<T> items) {
        return CollectionOp.lazyAdd(ArrayDeque::new, source, items);
    }

    public static <T> void lazyConsumers(ArrayDeque<Consumer<T>> consumers, T item) {
        if (consumers == null || consumers.isEmpty()) {
            return;
        }
        for (Consumer<T> cons : consumers) {
            SafeOpt.ofNullable(cons).ifPresent(func -> func.accept(item)).throwIfErrorNested();
        }
    }

    public static <T> void lazyPredicates(ArrayDeque<Function<T, Predicate>> predMakers, T item, List<Predicate> collector) {
        if (predMakers == null || predMakers.isEmpty()) {
            return;
        }
        for (Function<T, Predicate> maker : predMakers) {
            SafeOpt.ofNullable(maker).map(func -> func.apply(item))
                    .ifPresent(collector::add).throwIfErrorNested();
        }
    }

    public <T> M withPred(SingularAttribute<T_ROOT, T> att, BiFunction<CriteriaBuilder, Expression<T>, Predicate> func) {
        Objects.requireNonNull(func);
        Function<Phase2<T_ROOT>, Predicate> fun = (Phase2<T_ROOT> t) -> func.apply(t.cb(), t.root().get(att));
        M of = me();
        of.pred2 = lazyAdd(of.pred2, fun);
        return of;
    }

    public <T, C extends Collection<T>> M withPred(PluralAttribute<T_ROOT, C, T> att, BiFunction<CriteriaBuilder, Expression<C>, Predicate> func) {
        Objects.requireNonNull(func);
        Function<Phase2<T_ROOT>, Predicate> fun = (Phase2<T_ROOT> t) -> func.apply(t.cb(), t.root().get(att));
        M of = me();
        of.pred2 = lazyAdd(of.pred2, fun);
        return of;
    }

    public <K, V, MAP extends Map<K, V>> M withPred(MapAttribute<T_ROOT, K, V> att, BiFunction<CriteriaBuilder, Expression<MAP>, Predicate> func) {
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

    public <RES> M withSubqueryPred(BaseJpaQueryDecor<T_ROOT, RES, ?> decor, Function<Subquery<RES>, Predicate> func) {
        Objects.requireNonNull(decor);
        Objects.requireNonNull(func);
        Function<Phase3Query<T_ROOT, T_RESULT>, Predicate> fun = (Phase3Query<T_ROOT, T_RESULT> t) -> {
            Subquery<RES> subquery = t.query().subquery(decor.resultClass);
            subquery = decor.decorateSubquery(t.em(), subquery);
            return func.apply(subquery);
        };
        M of = me();
        of.pred3Query = lazyAdd(of.pred3Query, fun);
        return of;
    }
    
    public <RES> M withSubquery(BaseJpaQueryDecor<T_ROOT, RES, ?> decor, Function<Phase3Subquery<T_ROOT,RES>, Predicate> func) {
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
        of.pred3Query = lazyAdd(of.pred3Query, fun);
        return of;
    }
    
    public <RES> M withSubquery(BaseJpaQueryDecor<T_ROOT, RES, ?> decor, Consumer<Phase3Subquery<T_ROOT,RES>> cons) {
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
        of.dec3Query = lazyAdd(of.dec3Query, fun);
        return of;
    }

    public M withPredQuery(Function<Phase3Query<T_ROOT, T_RESULT>, Predicate> func) {
        Objects.requireNonNull(func);
        M of = me();
        of.pred3Query = lazyAdd(of.pred3Query, func);
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

    public M withDec3Query(Consumer<Phase3Query<T_ROOT, T_RESULT>> cons) {
        Objects.requireNonNull(cons);
        M of = me();
        of.dec3Query = lazyAdd(of.dec3Query, cons);
        return of;
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

    public Stream<T_RESULT> buildStream(EntityManager em) {
        return build(em).getResultList().stream();
    }

    public List<T_RESULT> buildList(EntityManager em) {
        return build(em).getResultList();
    }

    public TypedQuery<T_RESULT> build(EntityManager em) {
        TypedQuery<T_RESULT> typed = em.createQuery(decorateQuery(em));

        Phase4<T_RESULT> p4 = DecoratorPhases.of(typed);
        lazyConsumers(dec4, p4);
        return typed;
    }

    public CriteriaQuery<T_RESULT> decorateQuery(EntityManager em) {
        Objects.requireNonNull(em);
        CriteriaBuilder builder = em.getCriteriaBuilder();

        Phase1 p1 = DecoratorPhases.of(em, builder);
        lazyConsumers(dec1, p1);

        CriteriaQuery<T_RESULT> query = builder.createQuery(resultClass);
        Root<T_ROOT> root = query.from(rootClass);
        ArrayList<Predicate> predicates = new ArrayList<>();

        Phase2<T_ROOT> p2 = DecoratorPhases.of(p1, root);
        lazyConsumers(dec2, p2);
        lazyPredicates(pred2, p2, predicates);

        Phase3Query<T_ROOT, T_RESULT> p3 = DecoratorPhases.of(p2, query);
        lazyConsumers(dec3Query, p3);
        lazyPredicates(pred3Query, p3, predicates);

        query.where(predicates.stream().toArray(s -> new Predicate[s]));

        return query.select(selection.apply(p2));

    }

    public <PARENT_ROOT> Subquery<T_RESULT> decorateSubquery(EntityManager em, AbstractQuery<PARENT_ROOT> parentQuery) {
        Objects.requireNonNull(em);
        Objects.requireNonNull(parentQuery);
        CriteriaBuilder builder = em.getCriteriaBuilder();

        Phase1 p1 = DecoratorPhases.of(em, builder);
        lazyConsumers(dec1, p1);

        ArrayList<Predicate> predicates = new ArrayList<>();
        Subquery<T_RESULT> subquery = parentQuery.subquery(resultClass);
        Root<T_ROOT> root = subquery.from(rootClass);

        Phase2<T_ROOT> p2 = DecoratorPhases.of(p1, root);
        lazyConsumers(dec2, p2);
        lazyPredicates(pred2, p2, predicates);

        Phase3Subquery<T_ROOT, T_RESULT> p3 = DecoratorPhases.of(p2, subquery, parentQuery);
        lazyConsumers(dec3Subquery, p3);
        lazyPredicates(pred3Subquery, p3, predicates);

        subquery.where(predicates.stream().toArray(s -> new Predicate[s]));

        return subquery.select(selection.apply(p2));

    }
}
