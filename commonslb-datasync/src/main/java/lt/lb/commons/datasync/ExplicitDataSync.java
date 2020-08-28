package lt.lb.commons.datasync;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import lt.lb.commons.F;
import lt.lb.commons.containers.values.SetOnce;
import lt.lb.commons.containers.values.ValueProxy;

/**
 *
 * @author laim0nas100
 */
public abstract class ExplicitDataSync<P, M, D, V extends Valid<M>> extends BaseValidation<M, V> implements DataSyncManagedValidation<P, M, D, V> {

    protected SetOnce<Supplier<? extends D>> displaySupp = new SetOnce<>();
    protected SetOnce<Consumer<? super D>> displaySync = new SetOnce<>();
    protected SetOnce<Supplier<? extends P>> persistenceSupp = new SetOnce<>();
    protected SetOnce<Consumer<? super P>> persistenceSync = new SetOnce<>();
    protected SetOnce<Function<? super P, ? extends M>> persistGet = new SetOnce<>();
    protected SetOnce<Function<? super M, ? extends P>> persistSet = new SetOnce<>();
    protected SetOnce<Function<? super D, ? extends M>> displayGet = new SetOnce<>();
    protected SetOnce<Function<? super M, ? extends D>> displaySet = new SetOnce<>();

    protected M managed;

    /**
     * Gateway to put data formatted for display layer
     *
     * @param displaySync
     */
    @Override
    public void withDisplaySync(Consumer<? super D> displaySync) {
        this.displaySync.set(displaySync);
    }

    /**
     * Gateway to put data formatted for persistence layer
     *
     * @param persSync
     */
    @Override
    public void withPersistSync(Consumer<? super P> persSync) {
        this.persistenceSync.set(persSync);
    }

    public void withPersistProxy(ValueProxy<P> proxy) {
        this.persistenceSupp.set(proxy);
        this.persistenceSync.set(proxy);
    }

    public void withDisplayProxy(ValueProxy<D> proxy) {
        this.displaySupp.set(proxy);
        this.displaySync.set(proxy);
    }

    /**
     * A gateway to extract data from display layer
     *
     * @param displaySup
     */
    @Override
    public void withDisplaySup(Supplier<? extends D> displaySup) {
        this.displaySupp.set(displaySup);
    }

    /**
     * A gateway to extract data from persistence layer
     *
     * @param persistSup
     */
    @Override
    public void withPersistSup(Supplier<? extends P> persistSup) {
        this.persistenceSupp.set(persistSup);
    }

    /**
     * Adapter to convert data from persistence layer to managed
     *
     * @param func
     */
    @Override
    public void withPersistGet(Function<? super P, ? extends M> func) {
        this.persistGet.set(func);
    }

    /**
     * Adapter to convert data from display to managed
     *
     * @param func
     */
    @Override
    public void withDisplayGet(Function<? super D, ? extends M> func) {
        this.displayGet.set(func);
    }

    /**
     * Adapter to convert data from managed to persistence layer
     *
     * @param func
     */
    @Override
    public void withPersistSet(Function<? super M, ? extends P> func) {
        this.persistSet.set(func);
    }

    /**
     * Adapter to convert data from managed to display layer
     *
     * @param func
     */
    @Override
    public void withDisplaySet(Function<? super M, ? extends D> func) {
        this.displaySet.set(func);
    }

    /**
     * Set managed value
     *
     * @param managed
     */
    @Override
    public void setManaged(M managed) {
        this.managed = managed;
    }

    /**
     * Get managed value
     *
     * @return
     */
    @Override
    public M getManaged() {
        return managed;
    }

    /**
     * Format the managed value and sync to the persistence gateway
     */
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

    /**
     * Format the managed value and sync to the display gateway
     */
    @Override
    public void syncDisplay() {
        if (this.displaySet.isNotNull() && this.displaySync.isNotNull()) {
            Function<? super M, ? extends D> toDisplay = this.displaySet.get();
            D newDisplay = toDisplay.apply(this.getManaged());
            this.displaySync.get().accept(newDisplay);
        } else {
            //explicitly launch to throw exceptions
            this.displaySet.get();
            this.displaySync.get();
        }
    }

    /**
     * Get the value from display layer, format it and set it to managed
     */
    @Override
    public void syncManagedFromDisplay() {
        if (this.displayGet.isNotNull() && this.displaySupp.isNotNull()) {
            Supplier<? extends D> get = this.displaySupp.get();
            D display = get.get();

            Function<? super D, ? extends M> toManaged = this.displayGet.get();
            M newManaged = toManaged.apply(display);
            this.setManaged(newManaged);

        } else {
            //explicitly launch to throw exceptions
            this.displayGet.get();
            this.displaySupp.get();
        }
    }

    /**
     * Get the value form persistence layer, format it and set it to managed
     */
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

    /**
     * Marks that display layer is the same as managed, i.e. there's no
     * conversion
     */
    public void withIdentityDisplay() {
        this.displaySet.set(v -> F.cast(v));
        this.displayGet.set(v -> F.cast(v));
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
