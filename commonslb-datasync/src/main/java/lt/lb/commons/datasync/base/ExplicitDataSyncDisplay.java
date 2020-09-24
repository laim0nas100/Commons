package lt.lb.commons.datasync.base;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import lt.lb.commons.F;
import lt.lb.commons.containers.values.SetOnce;
import lt.lb.commons.containers.values.Value;
import lt.lb.commons.containers.values.ValueProxy;
import lt.lb.commons.datasync.DataSyncDisplay;

/**
 *
 * @author laim0nas100
 */
public abstract class ExplicitDataSyncDisplay <D,M> implements DataSyncDisplay<D,M> {

    public ExplicitDataSyncDisplay(ValueProxy<D> displaySync) {
        withDisplayProxy(displaySync);
    }

    public ExplicitDataSyncDisplay() {
    }

    
    protected SetOnce<Supplier<? extends D>> displaySupp = new SetOnce<>();
    protected SetOnce<Consumer<? super D>> displaySync = new SetOnce<>();
    protected SetOnce<Function<? super D, ? extends M>> displayGet = new SetOnce<>();
    protected SetOnce<Function<? super M, ? extends D>> displaySet = new SetOnce<>();

    
    @Override
    public void withDisplaySync(Consumer<? super D> displaySync) {
        this.displaySync.set(displaySync);
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
    public void withDisplayGet(Function<? super D, ? extends M> func) {
        this.displayGet.set(func);
    }

    @Override
    public void withDisplaySet(Function<? super M, ? extends D> func) {
        this.displaySet.set(func);
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

    /**
     * Marks that display layer is the same as managed, i.e. there's no
     * conversion
     */
    public void withIdentityDisplay() {
        this.displaySet.set(v -> F.cast(v));
        this.displayGet.set(v -> F.cast(v));
    }

}