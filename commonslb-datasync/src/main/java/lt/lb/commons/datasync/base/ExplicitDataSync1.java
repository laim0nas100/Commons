package lt.lb.commons.datasync.base;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import lt.lb.commons.containers.values.ValueProxy;
import lt.lb.commons.datasync.DataSyncManagedValidation;
import lt.lb.commons.datasync.Valid;

/**
 *
 * @author laim0nas100
 */
public abstract class ExplicitDataSync1<P, M, D, V extends Valid<M>> extends BaseValidation<M, V> implements DataSyncManagedValidation<P, M, D, V> {

    protected ExplicitDataSync1<P, M, D, V> me = this;
    protected ExplicitDataSyncPersist<P, M> persist = new ExplicitDataSyncPersist<P, M>() {
        @Override
        public void setManaged(M managed) {
            me.setManaged(managed);
        }

        @Override
        public M getManaged() {
            return me.getManaged();
        }
    };
    protected ExplicitDataSyncDisplay<D, M> display = new ExplicitDataSyncDisplay<D, M>() {
        @Override
        public void setManaged(M managed) {
            me.setManaged(managed);
        }

        @Override
        public M getManaged() {
            return me.getManaged();
        }
    };
    protected M managed;

    @Override
    public void withDisplaySync(Consumer<? super D> displaySync) {
        this.display.withDisplaySync(displaySync);
    }

    @Override
    public void withPersistSync(Consumer<? super P> persSync) {
        this.persist.withPersistSync(persSync);
    }

    public void withPersistProxy(ValueProxy<P> proxy) {
        this.persist.withPersistProxy(proxy);
    }

    public void withDisplayProxy(ValueProxy<D> proxy) {
        this.display.withDisplayProxy(proxy);
    }

    @Override
    public void withDisplaySup(Supplier<? extends D> displaySup) {
        this.display.withDisplaySup(displaySup);
    }

    @Override
    public void withPersistSup(Supplier<? extends P> persistSup) {
        this.persist.withPersistSup(persistSup);
    }

    @Override
    public void withPersistGet(Function<? super P, ? extends M> func) {
        this.persist.withPersistGet(func);
    }

    @Override
    public void withDisplayGet(Function<? super D, ? extends M> func) {
        this.display.withDisplayGet(func);
    }

    @Override
    public void withPersistSet(Function<? super M, ? extends P> func) {
        this.persist.withPersistSet(func);
    }

    @Override
    public void withDisplaySet(Function<? super M, ? extends D> func) {
        this.display.withDisplaySet(func);
    }

    @Override
    public void setManaged(M managed) {
        this.managed = managed;
    }

    @Override
    public M getManaged() {
        return managed;
    }

    @Override
    public void syncPersist() {
        persist.syncPersist();
    }

    @Override
    public void syncDisplay() {
        display.syncDisplay();
    }

    @Override
    public void syncManagedFromDisplay() {
        display.syncManagedFromDisplay();
    }

    @Override
    public void syncManagedFromPersist() {
        persist.syncManagedFromPersist();
    }

    /**
     * Marks that persistence layer is the same as managed, i.e. there's no
     * conversion
     */
    public void withIdentityPersist() {
        persist.withIdentityPersist();
    }

    /**
     * Marks that display layer is the same as managed, i.e. there's no
     * conversion
     */
    public void withIdentityDisplay() {
        display.withIdentityDisplay();
    }

    /**
     * Marks display and persistence layers the same type as managed, i.e.
     * there's no conversion
     */
    public void withNoConversion() {
        withIdentityDisplay();
        withIdentityPersist();
    }

}
