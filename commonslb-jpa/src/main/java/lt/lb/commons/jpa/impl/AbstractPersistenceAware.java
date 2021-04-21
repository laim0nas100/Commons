package lt.lb.commons.jpa.impl;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.stream.Stream;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import lt.lb.commons.F;
import lt.lb.uncheckedutils.SafeOpt;
import lt.lb.uncheckedutils.func.UncheckedFunction;
import lt.lb.uncheckedutils.func.UncheckedSupplier;
import lt.lb.commons.jpa.EntityManagerAware;
import lt.lb.commons.jpa.ExtQuery;
import lt.lb.commons.jpa.JPACommands;
import lt.lb.commons.jpa.decorators.IQueryDecorator;
import lt.lb.commons.jpa.ids.IDFactory;
import lt.lb.uncheckedutils.NestedException;

public interface AbstractPersistenceAware extends JPACommands, EntityManagerAware {

    public abstract IDFactory getIds();

    @Override
    public default <T> T createPersistent(Class<T> cls) {
        T entity = this.createTransient(cls);
        getEntityManager().persist(entity);
        return entity;
    }

    @Override
    public default <T> T createTransient(Class<T> cls) {
        return F.uncheckedCall(() -> cls.getDeclaredConstructor().newInstance());
    }

    @Override
    public default <T> List<T> getAll(Class<T> cls) {
        CriteriaBuilder criteriaBuilder = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<T> query = criteriaBuilder.createQuery(cls);
        Root<T> from = query.from(cls);
        CriteriaQuery<T> select = query.select(from);
        TypedQuery<T> createQuery = getEntityManager().createQuery(select);
        return createQuery.getResultList();
    }

    @Override
    public default <T> Stream<T> getAllStream(Class<T> cls) {
        return getAll(cls).stream();
    }

    @Override
    public default <T> boolean persist(Class<T> cls, T item) {
        return this.persist(item);
    }

    @Override
    public default <T> boolean delete(T item) {

        if (item != null) {
            if (!getEntityManager().contains(item)) {
                item = update(item);
            }

            getEntityManager().remove(item);
            return true;
        }
        return false;
    }

    @Override
    public default <T> boolean delete(Class<T> cls, T item) {
        return delete(item);
    }

    public default <T> boolean isDetached(T entity) {
        return F.uncheckedCall(() -> {
            if (entity == null) {
                return true;
            }
            Object id = getIds().defaultGetId(entity);

            EntityManager em = getEntityManager();
            return id != null && !em.contains(entity) && this.find(entity.getClass(), id).isPresent();
        });

    }

    public default <T> boolean isTransient(T entity) {
        return F.uncheckedCall(() -> {
            if (entity == null) {
                return true;
            }
            Object id = getIds().defaultGetId(entity);

            EntityManager em = getEntityManager();
            return id == null && !em.contains(entity);
        });
    }

    @Override
    public default <T> T update(T item) {
        if (item == null) {
            return null;
        }
        EntityManager em = getEntityManager();
        if (isTransient(item)) {
            em.persist(item);
            return item;
        }
        if (isDetached(item)) {
            return em.merge(item);
        }
        if (em.contains(item)) {
            return item;
        } else {
            //with ID but not in base??
//            return em.merge(item);
            throw new IllegalArgumentException("Trying to save removed item, " + item);
        }
    }

    @Override
    public default <T> boolean persist(T item) {
        if (item == null) {
            return false;
        }
        if (isTransient(item)) {
            getEntityManager().persist(item);
        } else if (isDetached(item)) {
            getEntityManager().merge(item);
        }

        return true;
    }

    @Override
    public default <T> SafeOpt<T> find(Class<T> clz, Object primaryKey) {
        return SafeOpt.ofNullable(getEntityManager().find(clz, primaryKey));
    }

    @Override
    public default <T> void merge(T obj) {
        getEntityManager().merge(obj);
    }

    @Override
    public default <T> List<T> search(Class<T> clz, IQueryDecorator<T>... predicates) {
        EntityManager em = getEntityManager();
        CriteriaBuilder cb = em.getCriteriaBuilder();

        CriteriaQuery<T> q = cb.createQuery(clz);
        Root<T> root = q.from(clz);
        return this.decorate(q, cb, root, predicates).getResultList();
    }

    public default <X> ExtQuery<X> decorate(final CriteriaQuery<X> q, CriteriaBuilder cb, Root root, IQueryDecorator... predicates) {
        return new ExtQueryImpl<>(getEntityManager(), cb, q, root, predicates);
    }

    @Override
    public default <T> Long count(Class<T> clz, IQueryDecorator<T>... predicates) {
        EntityManager em = this.getEntityManager();
        CriteriaBuilder cb = em.getCriteriaBuilder();

        CriteriaQuery<Long> q = cb.createQuery(Long.class);
        Root<T> root = q.from(clz);
        q = q.select(cb.count(root));
        return this.decorate(q, cb, root, predicates).getSingleResult();

    }

    @Override
    public default <T> List<T> search(Class<T> clz, int start, int pageSize, IQueryDecorator<T>... predicates) {
        EntityManager em = this.getEntityManager();
        CriteriaBuilder cb = em.getCriteriaBuilder();

        CriteriaQuery<T> q = cb.createQuery(clz);
        Root<T> root = q.from(clz);
        ExtQuery<T> decorate = this.decorate(q, cb, root, predicates);
        return decorate.setFirstResult(start).setMaxResults(pageSize).getResultList();
    }

    public default Executor getAsyncExecutor() {
        return ForkJoinPool.commonPool();
    }

    @Override
    public default <T> Future<T> executeTransactionAsync(UncheckedFunction<EntityManager, T> supp) {
        UncheckedSupplier<T> decorated = () -> {
            EntityManagerFactory factory = getEntityManagerFactory();
            EntityManager em = null;

            try {
                em = factory.createEntityManager();
                em.getTransaction().begin();
                T value = supp.applyUnchecked(em);
                em.getTransaction().commit();
                return value;
            } catch (Throwable error) {
                if (em != null) {
                    em.getTransaction().rollback();
                }
                throw NestedException.of(error);
            } finally {
                if (em != null) {
                    em.close();
                }
            }
        };
        FutureTask<T> task = new FutureTask<>(decorated);
        getAsyncExecutor().execute(task);
        return task;
    }

}
