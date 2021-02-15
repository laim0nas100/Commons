package lt.lb.commons.jpa.querydecor;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.AbstractQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

/**
 *
 * Simple way to combine various parts when constructing a JPA CriteriaQuery
 * using {@link BaseJpaQueryDecor} or any other variant, without polluting the
 * argument space.
 *
 * @author laim0nas100
 */
public interface DecoratorPhases {

    public static Phase1 of(EntityManager em, CriteriaBuilder cb) {
        return new Phase1() {
            @Override
            public EntityManager em() {
                return em;
            }

            @Override
            public CriteriaBuilder cb() {
                return cb;
            }
        };
    }

    public static <T> Phase2<T> of(EntityManager em, CriteriaBuilder cb, Root<T> root) {
        return new Phase2<T>() {
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
        };
    }

    public static <T> Phase2<T> of(Phase1 p1, Root<T> root) {
        return new Phase2<T>() {
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
        };
    }

    public static <T, R> Phase3Query<T, R> of(EntityManager em, CriteriaBuilder cb, Root<T> root, CriteriaQuery<R> query) {
        return new Phase3Query<T, R>() {
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
        };
    }

    public static <T, R> Phase3Query<T, R> of(Phase2<T> p2, CriteriaQuery<R> query) {
        return new Phase3Query<T, R>() {
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
        };
    }

    public static <T, R> Phase3Subquery<T, R> of(EntityManager em, CriteriaBuilder cb, Root<T> root, Subquery<R> query, AbstractQuery<?> parent) {
        return new Phase3Subquery<T, R>() {
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
            public Subquery<R> subquery() {
                return query;
            }

            @Override
            public AbstractQuery<?> parent() {
                return parent;
            }
        };
    }

    public static <T, R> Phase3Subquery<T, R> of(EntityManager em, CriteriaBuilder cb, Root<T> root, Subquery<R> query) {
        return new Phase3Subquery<T, R>() {
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
            public Subquery<R> subquery() {
                return query;
            }
        };
    }

    public static <T, R> Phase3Subquery<T, R> of(Phase2<T> p2, Subquery<R> query, AbstractQuery<?> parent) {
        return new Phase3Subquery<T, R>() {
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
            public Subquery<R> subquery() {
                return query;
            }

            @Override
            public AbstractQuery<?> parent() {
                return parent;
            }

        };
    }

    public static <T, R> Phase3Subquery<T, R> of(Phase2<T> p2, Subquery<R> query) {
        return new Phase3Subquery<T, R>() {
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
            public Subquery<R> subquery() {
                return query;
            }
        };
    }

    public static <T> Phase4<T> of(TypedQuery<T> query) {
        return () -> query;
    }

    public static interface Phase1 {

        public EntityManager em();

        public CriteriaBuilder cb();
    }

    public static interface Phase2<T> extends Phase1 {

        public Root<T> root();
    }

    public static interface Phase3Query<T, R> extends Phase2<T> {

        public CriteriaQuery<R> query();
    }

    public static interface Phase3Subquery<T, R> extends Phase2<T> {

        public default AbstractQuery<?> parent() {
            return subquery().getParent();
        }

        public default CriteriaQuery<?> parentQuery() {
            return (CriteriaQuery<?>) parent();
        }

        public default Subquery<?> parentSubquery() {
            return (Subquery<?>) parent();
        }

        public default boolean parentIsQuery() {
            return parent() instanceof CriteriaQuery;
        }

        public default boolean parentIsSubquery() {
            return parent() instanceof Subquery;
        }

        public Subquery<R> subquery();
    }

    public static interface Phase4<T> {

        public TypedQuery<T> typedQuery();
    }

}
