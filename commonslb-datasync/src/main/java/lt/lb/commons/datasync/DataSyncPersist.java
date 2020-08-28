package lt.lb.commons.datasync;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 *
 * @author laim0nas100
 */
public interface DataSyncPersist<P, M> extends SyncManaged<M> {

    public void withPersistSync(Consumer<? super P> persSync);

    public void withPersistSup(Supplier<? extends P> persistSup);

    public void withPersistGet(Function<? super P, ? extends M> func);

    public void withPersistSet(Function<? super M, ? extends P> func);

    public void syncPersist();

    public void syncManagedFromPersist();
}
