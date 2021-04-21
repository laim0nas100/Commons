package lt.lb.commons.jpa.impl;

import java.util.List;
import java.util.stream.Stream;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import lt.lb.uncheckedutils.SafeOpt;
import lt.lb.commons.jpa.EntityFacade;
import lt.lb.commons.jpa.ExtQuery;
import lt.lb.commons.jpa.decorators.IQueryDecorator;

/**
 * Every method is wrapped in {@link EntityFacade#executeTransaction(lt.lb.uncheckedutils.func.UncheckedSupplier)
 * }, so the implementation of this method is important to establish a
 * transaction.
 *
 * @author laim0nas100
 */
public interface TrasactionDelegatingEntityFacade extends AbstractPersistenceAware {

    @Override
    public default <T> List<T> search(Class<T> clz, int start, int pageSize, IQueryDecorator<T>... predicates) {
        return executeTransaction(() -> {
            return AbstractPersistenceAware.super.search(clz, start, pageSize, predicates);
        });
    }

    @Override
    public default <T> Long count(Class<T> clz, IQueryDecorator<T>... predicates) {
        return executeTransaction(() -> {
            return AbstractPersistenceAware.super.count(clz, predicates);
        });
    }

    @Override
    public default <X> ExtQuery<X> decorate(CriteriaQuery<X> q, CriteriaBuilder cb, Root root, IQueryDecorator... predicates) {
        return executeTransaction(() -> {
            return AbstractPersistenceAware.super.decorate(q, cb, root, predicates);
        });
    }

    @Override
    public default <T> List<T> search(Class<T> clz, IQueryDecorator<T>... predicates) {
        return executeTransaction(() -> {
            return AbstractPersistenceAware.super.search(clz, predicates);
        });
    }

    @Override
    public default <T> void merge(T obj) {
        executeTransaction(() -> {
            AbstractPersistenceAware.super.merge(obj);
        });
    }

    @Override
    public default <T> SafeOpt<T> find(Class<T> clz, Object primaryKey) {
        return executeTransaction(() -> {
            return AbstractPersistenceAware.super.find(clz, primaryKey);
        });
    }

    @Override
    public default <T> boolean persist(T item) {
        return executeTransaction(() -> {
            return AbstractPersistenceAware.super.persist(item);
        });
    }

    @Override
    public default <T> T update(T item) {
        return executeTransaction(() -> {
            return AbstractPersistenceAware.super.update(item);
        });
    }

    @Override
    public default <T> boolean isTransient(T entity) {
        return executeTransaction(() -> {
            return AbstractPersistenceAware.super.isTransient(entity);
        });
    }

    @Override
    public default <T> boolean isDetached(T entity) {
        return executeTransaction(() -> {
            return AbstractPersistenceAware.super.isDetached(entity);
        });
    }

    @Override
    public default <T> boolean delete(Class<T> cls, T item) {
        return executeTransaction(() -> {
            return AbstractPersistenceAware.super.delete(cls, item);
        });
    }

    @Override
    public default <T> boolean delete(T item) {
        return executeTransaction(() -> {
            return AbstractPersistenceAware.super.delete(item);
        });
    }

    @Override
    public default <T> boolean persist(Class<T> cls, T item) {
        return executeTransaction(() -> {
            return AbstractPersistenceAware.super.persist(cls, item);
        });
    }

    @Override
    public default <T> Stream<T> getAllStream(Class<T> cls) {
        return executeTransaction(() -> {
            return AbstractPersistenceAware.super.getAllStream(cls);
        });
    }

    @Override
    public default <T> List<T> getAll(Class<T> cls) {
        return executeTransaction(() -> {
            return AbstractPersistenceAware.super.getAll(cls);
        });
    }

    @Override
    public default <T> T createTransient(Class<T> cls) {
        return executeTransaction(() -> {
            return AbstractPersistenceAware.super.createTransient(cls);
        });
    }

    @Override
    public default <T> T createPersistent(Class<T> cls) {
        return executeTransaction(() -> {
            return AbstractPersistenceAware.super.createPersistent(cls);
        });
    }
}
