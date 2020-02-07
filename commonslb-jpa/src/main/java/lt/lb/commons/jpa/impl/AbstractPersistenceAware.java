package lt.lb.commons.jpa.impl;

import java.util.List;
import java.util.stream.Stream;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import lt.lb.commons.F;
import lt.lb.commons.SafeOpt;
import lt.lb.commons.func.unchecked.UnsafeRunnable;
import lt.lb.commons.jpa.EntityManagerAware;
import lt.lb.commons.jpa.ExtQuery;
import lt.lb.commons.jpa.JPACommands;
import lt.lb.commons.jpa.decorators.IQueryDecorator;
import lt.lb.commons.jpa.ids.IDFactory;

public abstract class AbstractPersistenceAware implements JPACommands, EntityManagerAware {


    public abstract IDFactory getIds();
    
    @Override
    public void executeTransaction(UnsafeRunnable run) {
        F.unsafeRun(run);
    }

    @Override
    public <T> T createPersistent(Class<T> cls) {
        T entity = this.createTransient(cls);
        getEntityManager().persist(entity);
        return entity;
    }

    @Override
    public <T> T createTransient(Class<T> cls) {
        return F.unsafeCall(() -> cls.getDeclaredConstructor().newInstance());
    }

    @Override
    public <T> List<T> getAll(Class<T> cls) {
        CriteriaBuilder criteriaBuilder = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<T> query = criteriaBuilder.createQuery(cls);
        Root<T> from = query.from(cls);
        CriteriaQuery<T> select = query.select(from);
        TypedQuery<T> createQuery = getEntityManager().createQuery(select);
        return createQuery.getResultList();
    }

    @Override
    public <T> Stream<T> getAllStream(Class<T> cls) {
        CriteriaBuilder criteriaBuilder = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<T> query = criteriaBuilder.createQuery(cls);
        Root<T> from = query.from(cls);
        CriteriaQuery<T> select = query.select(from);
        TypedQuery<T> createQuery = getEntityManager().createQuery(select);
        return createQuery.getResultStream();
    }

    @Override
    public <T> boolean persist(Class<T> cls, T item) {
        return this.persist(item);
    }

    @Override
    public <T> boolean delete(T item) {
        if (item != null) {
            getEntityManager().remove(item);
            return true;
        }
        return false;
    }

    @Override
    public <T> boolean delete(Class<T> cls, T item) {
        return delete(item);
    }

    public <T> boolean isDetached(T entity) {
        return F.unsafeCall(() -> {
            if (entity == null) {
                return true;
            }
            Object id = getIds().defaultGetId(entity);

            EntityManager em = getEntityManager();
            return id != null && !em.contains(entity) && this.find(entity.getClass(), id).isPresent();
        });

    }

    public <T> boolean isTransient(T entity) {
        return F.unsafeCall(() -> {
            if (entity == null) {
                return true;
            }
            Object id = getIds().defaultGetId(entity);

            EntityManager em = getEntityManager();
            return id == null && !em.contains(entity);
        });
    }

    @Override
    public <T> T update(T item) {
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
            throw new IllegalArgumentException("Can't resolve state of " + item);
        }
    }

    @Override
    public <T> boolean persist(T item) {
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
    public <T> SafeOpt<T> find(Class<T> clz, Object primaryKey) {
        return SafeOpt.ofNullable(getEntityManager().find(clz, primaryKey));
    }

    @Override
    public <T> void merge(T obj) {
        getEntityManager().merge(obj);
    }

    @Override
    public <T> List<T> search(Class<T> clz, IQueryDecorator<T>... predicates) {
        EntityManager em = getEntityManager();
        CriteriaBuilder cb = em.getCriteriaBuilder();

        CriteriaQuery<T> q = cb.createQuery(clz);
        Root<T> root = q.from(clz);
        return this.decorate(q, cb, root, predicates).getResultList();
    }

    protected abstract <X> ExtQuery<X> decorate(final CriteriaQuery<X> q, CriteriaBuilder cb, Root root, IQueryDecorator... predicates);

    @Override
    public <T> Long count(Class<T> clz, IQueryDecorator<T>... predicates) {
        EntityManager em = this.getEntityManager();
        CriteriaBuilder cb = em.getCriteriaBuilder();

        CriteriaQuery<Long> q = cb.createQuery(Long.class);
        Root<T> root = q.from(clz);
        q = q.select(cb.count(root));
        return this.decorate(q, cb, root, predicates).getSingleResult();

    }

    @Override
    public <T> List<T> search(Class<T> clz, int start, int pageSize, IQueryDecorator<T>... predicates) {
        EntityManager em = this.getEntityManager();
        CriteriaBuilder cb = em.getCriteriaBuilder();

        CriteriaQuery<T> q = cb.createQuery(clz);
        Root<T> root = q.from(clz);
        ExtQuery<T> decorate = this.decorate(q, cb, root, predicates);
        return decorate.setFirstResult(start).setMaxResults(pageSize).getResultList();
    }

}
