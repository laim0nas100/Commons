package lt.lb.commons.jpa;

import java.util.List;
import java.util.Map;
import javax.persistence.EntityGraph;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.FlushModeType;
import javax.persistence.LockModeType;
import javax.persistence.Query;
import javax.persistence.StoredProcedureQuery;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.CriteriaUpdate;
import javax.persistence.metamodel.Metamodel;

/**
 *
 * @author laim0nas100
 */
public interface DelegatedEntityManager extends EntityManager {

    public EntityManager delegatedEM();

    @Override
    public default void persist(Object entity) { 
        delegatedEM().persist(entity);
    }

    @Override
    public default <T> T merge(T entity) {
        return delegatedEM().merge(entity);
    }

    @Override
    public default void remove(Object entity) {
        delegatedEM().remove(entity);
    }

    @Override
    public default <T> T find(Class<T> entityClass, Object primaryKey) {
        return delegatedEM().find(entityClass, primaryKey);
    }

    @Override
    public default <T> T find(Class<T> entityClass, Object primaryKey, Map<String, Object> properties) {
        return delegatedEM().find(entityClass, primaryKey, properties);
    }

    @Override
    public default <T> T find(Class<T> entityClass, Object primaryKey, LockModeType lockMode) {
        return delegatedEM().find(entityClass, primaryKey, lockMode);
    }

    @Override
    public default <T> T find(Class<T> entityClass, Object primaryKey, LockModeType lockMode, Map<String, Object> properties) {
        return delegatedEM().find(entityClass, primaryKey, lockMode, properties);
    }

    @Override
    public default <T> T getReference(Class<T> entityClass, Object primaryKey) {
        return delegatedEM().getReference(entityClass, primaryKey);
    }

    @Override
    public default void flush() {
        delegatedEM().flush();
    }

    @Override
    public default void setFlushMode(FlushModeType flushMode) {
        delegatedEM().setFlushMode(flushMode);
    }

    @Override
    public default FlushModeType getFlushMode() {
        return delegatedEM().getFlushMode();
    }

    @Override
    public default void lock(Object entity, LockModeType lockMode) {
        delegatedEM().lock(entity, lockMode);
    }

    @Override
    public default void lock(Object entity, LockModeType lockMode, Map<String, Object> properties) {
        delegatedEM().lock(entity, lockMode, properties);
    }

    @Override
    public default void refresh(Object entity) {
        delegatedEM().refresh(entity);
    }

    @Override
    public default void refresh(Object entity, Map<String, Object> properties) {
        delegatedEM().refresh(entity, properties);
    }

    @Override
    public default void refresh(Object entity, LockModeType lockMode) {
        delegatedEM().refresh(entity, lockMode);
    }

    @Override
    public default void refresh(Object entity, LockModeType lockMode, Map<String, Object> properties) {
        delegatedEM().refresh(entity, lockMode, properties);
    }

    @Override
    public default void clear() {
        delegatedEM().clear();
    }

    @Override
    public default void detach(Object entity) {
        delegatedEM().detach(entity);
    }

    @Override
    public default boolean contains(Object entity) {
        return delegatedEM().contains(entity);
    }

    @Override
    public default LockModeType getLockMode(Object entity) {
        return delegatedEM().getLockMode(entity);
    }

    @Override
    public default void setProperty(String propertyName, Object value) {
        delegatedEM().setProperty(propertyName, value);
    }

    @Override
    public default Map<String, Object> getProperties() {
        return delegatedEM().getProperties();
    }

    @Override
    public default Query createQuery(String qlString) {
        return delegatedEM().createQuery(qlString);
    }

    @Override
    public default <T> TypedQuery<T> createQuery(CriteriaQuery<T> criteriaQuery) {
        return delegatedEM().createQuery(criteriaQuery);
    }

    @Override
    public default Query createQuery(CriteriaUpdate updateQuery) {
        return delegatedEM().createQuery(updateQuery);
    }

    @Override
    public default Query createQuery(CriteriaDelete deleteQuery) {
        return delegatedEM().createQuery(deleteQuery);
    }

    @Override
    public default <T> TypedQuery<T> createQuery(String qlString, Class<T> resultClass) {
        return delegatedEM().createQuery(qlString, resultClass);
    }

    @Override
    public default Query createNamedQuery(String name) {
        return delegatedEM().createNamedQuery(name);
    }

    @Override
    public default <T> TypedQuery<T> createNamedQuery(String name, Class<T> resultClass) {
        return delegatedEM().createNamedQuery(name, resultClass);
    }

    @Override
    public default Query createNativeQuery(String sqlString) {
        return delegatedEM().createNativeQuery(sqlString);
    }

    @Override
    public default Query createNativeQuery(String sqlString, Class resultClass) {
        return delegatedEM().createNativeQuery(sqlString, resultClass);
    }

    @Override
    public default Query createNativeQuery(String sqlString, String resultSetMapping) {
        return delegatedEM().createNativeQuery(sqlString, resultSetMapping);
    }

    @Override
    public default StoredProcedureQuery createNamedStoredProcedureQuery(String name) {
        return delegatedEM().createNamedStoredProcedureQuery(name);
    }

    @Override
    public default StoredProcedureQuery createStoredProcedureQuery(String procedureName) {
        return delegatedEM().createStoredProcedureQuery(procedureName);
    }

    @Override
    public default StoredProcedureQuery createStoredProcedureQuery(String procedureName, Class... resultClasses) {
        return delegatedEM().createStoredProcedureQuery(procedureName, resultClasses);
    }

    @Override
    public default StoredProcedureQuery createStoredProcedureQuery(String procedureName, String... resultSetMappings) {
        return delegatedEM().createStoredProcedureQuery(procedureName, resultSetMappings);
    }

    @Override
    public default void joinTransaction() {
        delegatedEM().joinTransaction();
    }

    @Override
    public default boolean isJoinedToTransaction() {
        return delegatedEM().isJoinedToTransaction();
    }

    @Override
    public default <T> T unwrap(Class<T> cls) {
        return delegatedEM().unwrap(cls);
    }

    @Override
    public default Object getDelegate() {
        return delegatedEM().getDelegate();
    }

    @Override
    public default void close() {
        delegatedEM().close();
    }

    @Override
    public default boolean isOpen() {
        return delegatedEM().isOpen();
    }

    @Override
    public default EntityTransaction getTransaction() {
        return delegatedEM().getTransaction();
    }

    @Override
    public default EntityManagerFactory getEntityManagerFactory() {
        return delegatedEM().getEntityManagerFactory();
    }

    @Override
    public default CriteriaBuilder getCriteriaBuilder() {
        return delegatedEM().getCriteriaBuilder();
    }

    @Override
    public default Metamodel getMetamodel() {
        return delegatedEM().getMetamodel();
    }

    @Override
    public default <T> EntityGraph<T> createEntityGraph(Class<T> rootType) {
        return delegatedEM().createEntityGraph(rootType);
    }

    @Override
    public default EntityGraph<?> createEntityGraph(String graphName) {
        return delegatedEM().createEntityGraph(graphName);
    }

    @Override
    public default EntityGraph<?> getEntityGraph(String graphName) {
        return delegatedEM().getEntityGraph(graphName);
    }

    @Override
    public default <T> List<EntityGraph<? super T>> getEntityGraphs(Class<T> entityClass) {
        return delegatedEM().getEntityGraphs(entityClass);
    }

}
