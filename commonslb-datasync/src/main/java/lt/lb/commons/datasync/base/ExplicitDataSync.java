package lt.lb.commons.datasync.base;

import lt.lb.commons.datasync.base.BaseValidation;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import lt.lb.commons.F;
import lt.lb.commons.containers.values.SetOnce;
import lt.lb.commons.containers.values.ValueProxy;
import lt.lb.commons.datasync.DataSyncManagedValidation;
import lt.lb.commons.datasync.Valid;

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

    
    @Override
    public void withDisplaySync(Consumer<? super D> displaySync) {
        this.displaySync.set(displaySync);
    }

    
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

    
    @Override
    public void withDisplaySup(Supplier<? extends D> displaySup) {
        this.displaySupp.set(displaySup);
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
    public void withDisplayGet(Function<? super D, ? extends M> func) {
        this.displayGet.set(func);
    }

    
    @Override
    public void withPersistSet(Function<? super M, ? extends P> func) {
        this.persistSet.set(func);
    }

    
    @Override
    public void withDisplaySet(Function<? super M, ? extends D> func) {
        this.displaySet.set(func);
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
