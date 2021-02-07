package lt.lb.commons.jpa.impl;

import java.util.List;
import java.util.stream.Stream;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import lt.lb.commons.SafeOpt;
import lt.lb.commons.jpa.ExtQuery;
import lt.lb.commons.jpa.decorators.IQueryDecorator;
import lt.lb.commons.jpa.ids.IDFactory;

/**
 *
 * @author laim0nas100
 */
public abstract class TrasactionDelegatingEntityFacade extends AbstractPersistenceAware {

    @Override
    public <T> List<T> search(Class<T> clz, int start, int pageSize, IQueryDecorator<T>... predicates) {
        return executeTransaction(() -> {
            return super.search(clz, start, pageSize, predicates);
        });
    }

    @Override
    public <T> Long count(Class<T> clz, IQueryDecorator<T>... predicates) {
        return executeTransaction(() -> {
            return super.count(clz, predicates);
        });
    }

    @Override
    protected <X> ExtQuery<X> decorate(CriteriaQuery<X> q, CriteriaBuilder cb, Root root, IQueryDecorator... predicates) {
        return executeTransaction(() -> {
            return super.decorate(q, cb, root, predicates);
        });
    }

    @Override
    public <T> List<T> search(Class<T> clz, IQueryDecorator<T>... predicates) {
        return executeTransaction(() -> {
            return super.search(clz, predicates);
        });
    }

    @Override
    public <T> void merge(T obj) {
        executeTransaction(() -> {
            super.merge(obj);
        });
    }

    @Override
    public <T> SafeOpt<T> find(Class<T> clz, Object primaryKey) {
        return executeTransaction(() -> {
            return super.find(clz, primaryKey);
        });
    }

    @Override
    public <T> boolean persist(T item) {
        return executeTransaction(() -> {
            return super.persist(item);
        });
    }

    @Override
    public <T> T update(T item) {
        return executeTransaction(() -> {
            return super.update(item);
        });
    }

    @Override
    public <T> boolean isTransient(T entity) {
        return executeTransaction(() -> {
            return super.isTransient(entity);
        });
    }

    @Override
    public <T> boolean isDetached(T entity) {
        return executeTransaction(() -> {
            return super.isDetached(entity);
        });
    }

    @Override
    public <T> boolean delete(Class<T> cls, T item) {
        return executeTransaction(() -> {
            return super.delete(cls, item);
        });
    }

    @Override
    public <T> boolean delete(T item) {
        return executeTransaction(() -> {
            return super.delete(item);
        });
    }

    @Override
    public <T> boolean persist(Class<T> cls, T item) {
        return executeTransaction(() -> {
            return super.persist(cls, item);
        });
    }

    @Override
    public <T> Stream<T> getAllStream(Class<T> cls) {
        return executeTransaction(() -> {
            return super.getAllStream(cls);
        });
    }

    @Override
    public <T> List<T> getAll(Class<T> cls) {
        return executeTransaction(() -> {
            return super.getAll(cls);
        });
    }

    @Override
    public <T> T createTransient(Class<T> cls) {
        return executeTransaction(() -> {
            return super.createTransient(cls);
        });
    }

    @Override
    public <T> T createPersistent(Class<T> cls) {
        return executeTransaction(() -> {
            return super.createPersistent(cls);
        });

    }
    
    
    
    

    @Override
    public abstract IDFactory getIds();

}
