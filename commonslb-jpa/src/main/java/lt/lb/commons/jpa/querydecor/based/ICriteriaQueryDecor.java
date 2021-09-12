package lt.lb.commons.jpa.querydecor.based;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;
import javax.persistence.EntityManager;
import javax.persistence.NonUniqueResultException;
import javax.persistence.PersistenceException;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.SingularAttribute;
import lt.lb.commons.jpa.querydecor.DecoratorPhases;
import lt.lb.uncheckedutils.SafeOpt;

/**
 *
 * @author laim0nas100
 */
public interface ICriteriaQueryDecor<T_ROOT, T_RESULT, CTX, M extends ICriteriaQueryDecor<T_ROOT, T_RESULT, CTX, M>> extends IAbstractQueryDecor<T_ROOT, T_RESULT, CTX, M>, QuerySupplier {

    public M withPredQuery(boolean having, Function<DecoratorPhases.Phase3Query<T_ROOT, T_RESULT, CTX>, Predicate> func);

    public M withDec3Query(Consumer<DecoratorPhases.Phase3Query<T_ROOT, T_RESULT, CTX>> cons);


    @Override
    public TypedQuery<T_RESULT> build(EntityManager em);

    @Override
    public CriteriaQuery<T_RESULT> produceQuery(EntityManager em);

    public default M withPredQuery(Function<DecoratorPhases.Phase3Query<T_ROOT, T_RESULT, CTX>, Predicate> func) {
        return withPredQuery(false, func);
    }

    public default M setOrderBy(boolean asc, SingularAttribute<? super T_ROOT, ?>... att) {
        for (SingularAttribute attribute : att) {
            Objects.requireNonNull(attribute);
        }
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

    public default SafeOpt<T_RESULT> buildUniqueResult(EntityManager em) {
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

    public default T_RESULT buildUncheckedResult(EntityManager em) {
        return buildUniqueResult(em)
                .throwIfErrorUnwrapping(PersistenceException.class)
                .throwIfErrorRuntime()
                .throwNestedOrNull();
    }

    public default Stream<T_RESULT> buildStream(EntityManager em) {
        TypedQuery<T_RESULT> tq = build(em);
        try {
            return tq.getResultStream();
        } catch (UnsupportedOperationException unsupported) {
            return tq.getResultList().stream();
        }
    }

    public default Stream<T_RESULT> buildListStream(EntityManager em) {
        return build(em).getResultList().stream();
    }

    public default List<T_RESULT> buildList(EntityManager em) {
        return build(em).getResultList();
    }
}
