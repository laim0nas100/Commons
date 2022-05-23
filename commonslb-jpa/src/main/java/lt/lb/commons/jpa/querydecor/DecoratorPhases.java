package lt.lb.commons.jpa.querydecor;

import java.util.Objects;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.AbstractQuery;
import javax.persistence.criteria.CommonAbstractCriteria;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;
import lt.lb.commons.jpa.querydecor.LazyUtil.HiddenTypedQuery;

/**
 *
 * Simple way to combine various parts when constructing a JPA CriteriaQuery
 * using {@link BaseJpaQueryDecor} or any other variant, without polluting the
 * argument space.
 *
 * @author laim0nas100
 */
public interface DecoratorPhases {

    public static class DecoratedQueryWithFinalPhase<P extends Phase2, Q extends CommonAbstractCriteria> {
        public final P phase;
        public final Q query;

        public DecoratedQueryWithFinalPhase(P phase, Q query) {
            this.phase = phase;
            this.query = query;
        }
    }

    public static <CTX> Phase1<CTX> of(EntityManager em, CriteriaBuilder cb, CTX ctx) {
        return new Phase1<CTX>() {
            @Override
            public EntityManager em() {
                return em;
            }

            @Override
            public CriteriaBuilder cb() {
                return cb;
            }

            @Override
            public CTX ctx() {
                return ctx;
            }
        };
    }

    public static <T, CTX> Phase2<T, CTX> of(EntityManager em, CriteriaBuilder cb, Root<T> root, CTX ctx) {
        return new Phase2<T, CTX>() {
            @Override
            public Root<T> root() {
                return root;
            }

            @Override
            public CriteriaBuilder cb() {
                return cb;
            }

            @Override
            public EntityManager em() {
                return em;
            }

            @Override
            public CTX ctx() {
                return ctx;
            }
        };
    }

    public static <T, CTX> Phase2<T, CTX> of(Phase1<CTX> p1, Root<T> root) {
        return new Phase2<T, CTX>() {
            @Override
            public Root<T> root() {
                return root;
            }

            @Override
            public CriteriaBuilder cb() {
                return p1.cb();
            }

            @Override
            public EntityManager em() {
                return p1.em();
            }

            @Override
            public CTX ctx() {
                return p1.ctx();
            }
        };
    }

    public static <T, CTX> Phase3Common<T, CTX> of(Phase2<T, CTX> p1, CommonAbstractCriteria criteria) {
        return new Phase3Common<T, CTX>() {
            @Override
            public Root<T> root() {
                return p1.root();
            }

            @Override
            public CriteriaBuilder cb() {
                return p1.cb();
            }

            @Override
            public EntityManager em() {
                return p1.em();
            }

            @Override
            public CTX ctx() {
                return p1.ctx();
            }

            @Override
            public CommonAbstractCriteria query() {
                return criteria;
            }
        };
    }

    public static <T, R, CTX> Phase3Query<T, R, CTX> of(EntityManager em, CriteriaBuilder cb, Root<T> root, CriteriaQuery<R> query, CTX ctx) {
        return new Phase3Query<T, R, CTX>() {
            @Override
            public Root<T> root() {
                return root;
            }

            @Override
            public CriteriaBuilder cb() {
                return cb;
            }

            @Override
            public EntityManager em() {
                return em;
            }

            @Override
            public CriteriaQuery<R> query() {
                return query;
            }

            @Override
            public CTX ctx() {
                return ctx;
            }
        };
    }

    public static <T, R, CTX> Phase3Query<T, R, CTX> of(Phase2<T, CTX> p2, CriteriaQuery<R> query) {
        Objects.requireNonNull(p2);
        return new Phase3Query<T, R, CTX>() {
            @Override
            public Root<T> root() {
                return p2.root();
            }

            @Override
            public CriteriaBuilder cb() {
                return p2.cb();
            }

            @Override
            public EntityManager em() {
                return p2.em();
            }

            @Override
            public CriteriaQuery<R> query() {
                return query;
            }

            @Override
            public CTX ctx() {
                return p2.ctx();
            }
        };
    }

    public static <P, T, R, CTX> Phase3Subquery<P, T, R, CTX> of(EntityManager em, CriteriaBuilder cb, Root<P> parentRoot, Root<T> root, Subquery<R> query, AbstractQuery<?> parent, CTX ctx) {
        return new Phase3Subquery<P, T, R, CTX>() {

            @Override
            public Root<P> parentRoot() {
                return parentRoot;
            }

            @Override
            public Root<T> root() {
                return root;
            }

            @Override
            public CriteriaBuilder cb() {
                return cb;
            }

            @Override
            public EntityManager em() {
                return em;
            }

            @Override
            public Subquery<R> query() {
                return query;
            }

            @Override
            public AbstractQuery<?> parent() {
                return parent;
            }

            @Override
            public CTX ctx() {
                return ctx;
            }
        };
    }

    public static <P, T, R, CTX> Phase3Subquery<P, T, R, CTX> of(EntityManager em, CriteriaBuilder cb, Root<P> parent, Root<T> root, Subquery<R> query, CTX ctx) {
        return new Phase3Subquery<P, T, R, CTX>() {

            @Override
            public Root<P> parentRoot() {
                return parent;
            }

            @Override
            public Root<T> root() {
                return root;
            }

            @Override
            public CriteriaBuilder cb() {
                return cb;
            }

            @Override
            public EntityManager em() {
                return em;
            }

            @Override
            public Subquery<R> query() {
                return query;
            }

            @Override
            public CTX ctx() {
                return ctx;
            }
        };
    }

    public static <T, CTX> Phase4Typed<T, CTX> of(EntityManager em, TypedQuery<T> query, CTX ctx) {
        return new Phase4Typed<T, CTX>() {
            @Override
            public TypedQuery<T> query() {
                return query;
            }

            @Override
            public CTX ctx() {
                return ctx;
            }

            @Override
            public EntityManager em() {
                return em;
            }
        };
    }

    public static <CTX> Phase4<CTX> of(EntityManager em, Query query, CTX ctx) {

        return new Phase4<CTX>() {

            @Override
            public Query query() {
                return query;
            }

            @Override
            public CTX ctx() {
                return ctx;
            }

            @Override
            public EntityManager em() {
                return em;
            }
        };
    }

    public static <CTX, T> Phase4Typed<T, CTX> ofCastedType(EntityManager em, Query query, CTX ctx) {

        return new Phase4Typed<T, CTX>() {

            HiddenTypedQuery<T> hidden;

            @Override
            public TypedQuery<T> query() {
                if (hidden == null) {
                    hidden = new HiddenTypedQuery<>(query);
                }
                return hidden;
            }

            @Override
            public CTX ctx() {
                return ctx;
            }

            @Override
            public EntityManager em() {
                return em;
            }
        };
    }

    public static interface WithContext<CTX> {

        public CTX ctx();
    }

    public static interface WithEm<CTX> extends WithContext<CTX> {

        public EntityManager em();
    }

    public static interface Phase1<CTX> extends WithEm<CTX> {

        public CriteriaBuilder cb();

    }

    public static interface Phase2<T, CTX> extends Phase1<CTX> {

        public Root<T> root();
    }

    public static interface Phase3Common<T, CTX> extends Phase2<T, CTX> {

        public CommonAbstractCriteria query();

    }

    public static interface Phase3Abstract<T, R, CTX> extends Phase3Common<T, CTX> {

        @Override
        public AbstractQuery<R> query();

        public default Phase3Query<T, R, CTX> castToP3Query() {
            return (Phase3Query<T, R, CTX>) this;
        }

        public default Phase3Subquery<?, T, R, CTX> castToP3Subquery() {
            return (Phase3Subquery<?, T, R, CTX>) this;
        }
    }

    public static interface Phase3Query<T, R, CTX> extends Phase3Abstract<T, R, CTX> {

        @Override
        public CriteriaQuery<R> query();
    }

    public static interface Phase3Subquery<P, T, R, CTX> extends Phase3Abstract<T, R, CTX> {

        public Root<P> parentRoot();

        public default AbstractQuery<?> parent() {
            return query().getParent();
        }

        public default CriteriaQuery<?> castParentAsQuery() {
            return (CriteriaQuery<?>) parent();
        }

        public default Subquery<?> castParentAsSubquery() {
            return (Subquery<?>) parent();
        }

        public default boolean parentIsQuery() {
            return parent() instanceof CriteriaQuery;
        }

        public default boolean parentIsSubquery() {
            return parent() instanceof Subquery;
        }

        @Override
        public Subquery<R> query();
    }

    public static interface Phase4<CTX> extends WithEm<CTX> {

        public Query query();

    }

    public static interface Phase4Typed<T, CTX> extends Phase4<CTX> {

        @Override
        public TypedQuery<T> query();

    }

}
