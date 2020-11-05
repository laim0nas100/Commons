package lt.lb.commons.datasync.base;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import lt.lb.commons.F;
import lt.lb.commons.containers.values.Value;
import lt.lb.commons.containers.values.ValueProxy;
import lt.lb.commons.datasync.DataSyncPersist;

/**
 *
 * @author laim0nas100
 */
public abstract class ExplicitDataSyncPersist<P, M> implements DataSyncPersist<P, M> {

    protected Value<Supplier<? extends P>> persistenceSupp = new Value<>();
    protected Value<Consumer<? super P>> persistenceSync = new Value<>();
    protected Value<Function<? super P, ? extends M>> persistGet = new Value<>();
    protected Value<Function<? super M, ? extends P>> persistSet = new Value<>();

    public ExplicitDataSyncPersist(ValueProxy<P> persistSync) {
        withPersistProxy(persistSync);
    }

    public ExplicitDataSyncPersist() {
    }

    @Override
    public Consumer<? super P> getPersistSync() {
        return persistenceSync.get();
    }

    @Override
    public Supplier<? extends P> getPersistSup() {
        return persistenceSupp.get();
    }

    
    
    @Override
    public void withPersistSync(Consumer<? super P> persSync) {
        this.persistenceSync.set(persSync);
    }

    public void withPersistProxy(ValueProxy<P> proxy) {
        this.persistenceSupp.set(proxy);
        this.persistenceSync.set(proxy);
    }

    @Override
    public void withPersistSup(Supplier<? extends P> persistSup) {
        this.persistenceSupp.set(persistSup);
    }

    @Override
    public void withPersistGet(Function<? super P, ? extends M> func) {
        this.persistGet.set(func);
    }

    @Override
    public void withPersistSet(Function<? super M, ? extends P> func) {
        this.persistSet.set(func);
    }

    @Override
    public void syncPersist() {
        if (this.persistSet.isNotNull() && this.persistenceSync.isNotNull()) {
            Function<? super M, ? extends P> toPersist = this.persistSet.get();
            P newPersist = toPersist.apply(this.getManaged());
            this.persistenceSync.get().accept(newPersist);
        } else {
            //explicitly launch to throw exceptions
            this.persistSet.get();
            this.persistenceSync.get();
        }
    }

    @Override
    public void syncManagedFromPersist() {
        if (this.persistGet.isNotNull() && this.persistenceSupp.isNotNull()) {
            Supplier<? extends P> get = this.persistenceSupp.get();
            P persist = get.get();

            Function<? super P, ? extends M> toManaged = this.persistGet.get();
            M newManaged = toManaged.apply(persist);
            this.setManaged(newManaged);
        } else {
            //explicitly launch to throw exceptions
            persistGet.get();
            persistenceSupp.get();
        }
    }

    /**
     * Marks that persistence layer is the same as managed, i.e. there's no
     * conversion
     */
    public void withIdentityPersist() {
        this.persistSet.set(v -> F.cast(v));
        this.persistGet.set(v -> F.cast(v));
    }

}
