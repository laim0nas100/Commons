package lt.lb.commons.jpa;

import java.util.List;
import java.util.stream.Stream;
import javax.persistence.EntityManager;
import lt.lb.commons.SafeOpt;
import lt.lb.commons.containers.values.Value;
import lt.lb.commons.func.unchecked.UncheckedRunnable;
import lt.lb.commons.func.unchecked.UncheckedSupplier;
import lt.lb.commons.jpa.ids.ID;

/**
 *
 * @author laim0nas100 Facade to use interchangeably with Memory-saved and
 * JPA
 */
public interface EntityFacade {
    
    public EntityManager getEntityManager();
    
    public void executeTransaction(UncheckedRunnable run);
    
    public default <T> T executeTransaction(UncheckedSupplier<T> supp){
        Value<T> val = new Value<>();
        executeTransaction(()->{
            val.set(supp.get());
        });
        return val.get();
    }

    public <T> SafeOpt<T> find(Class<T> cls, Object key);
    
    public default <T> SafeOpt<T> findID(Class<T> cls, ID<? extends T, ?> key) {
        return find(cls, key.id);
    }

    public <T> T createPersistent(Class<T> cls);

    public <T> T createTransient(Class<T> cls);

    public <T> List<T> getAll(Class<T> cls);

    public <T> Stream<T> getAllStream(Class<T> cls);

    public <T> boolean persist(Class<T> cls, T item);

    public <T> boolean persist(T item);

    public <T> boolean delete(T item);

    public <T> boolean delete(Class<T> cls, T item);

    public default <T> T update(Class<T> cls, T item) {
        return this.update(item);
    }

    /**
     * All-in-one method to update new or changed instances
     * @param <T>
     * @param item
     * @return 
     */
    public <T> T update(T item);
    
}
