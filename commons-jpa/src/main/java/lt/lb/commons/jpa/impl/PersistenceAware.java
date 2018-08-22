package lt.lb.commons.jpa.impl;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import lt.lb.commons.jpa.EntityManagerAware;
import lt.lb.commons.jpa.ExtQuery;
import lt.lb.commons.jpa.JPACommands;
import lt.lb.commons.jpa.decorators.IQueryDecorator;

public abstract class PersistenceAware implements JPACommands, EntityManagerAware {

    @Override
    public <T> void persist(T obj) {
        getEntityManager().persist(obj);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T find(Class<?> clz, Object primaryKey) {
        return (T) getEntityManager().find(clz, primaryKey);
    }

    @Override
    public <T> void remove(Class<?> clz, Object primaryKey) {
        try {
            Object ref = getEntityManager().getReference(clz, primaryKey);
            getEntityManager().remove(ref);
        } catch (EntityNotFoundException e) {
        }
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
