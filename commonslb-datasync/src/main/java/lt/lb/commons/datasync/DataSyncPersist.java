package lt.lb.commons.datasync;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 *
 * @author laim0nas100
 */
public interface DataSyncPersist<P, M> extends SyncManaged<M>, SyncPersist {

    /**
     * Gateway to put data formatted for persistence layer
     *
     * @param persSync
     */
    public void withPersistSync(Consumer<? super P> persSync);
    
    public Consumer<? super P> getPersistSync();
    /**
     * A gateway to extract data from persistence layer
     *
     * @param persistSup
     */
    public void withPersistSup(Supplier<? extends P> persistSup);
    
    public Supplier<? extends P> getPersistSup();

    /**
     * Adapter to convert data from persistence layer to managed
     *
     * @param func
     */
    public void withPersistGet(Function<? super P, ? extends M> func);

    /**
     * Adapter to convert data from managed to persistence layer
     *
     * @param func
     */
    public void withPersistSet(Function<? super M, ? extends P> func);

}
