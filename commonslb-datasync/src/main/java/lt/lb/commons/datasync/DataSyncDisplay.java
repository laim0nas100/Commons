package lt.lb.commons.datasync;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 *
 * @author laim0nas100
 */
public interface DataSyncDisplay<D, M> extends SyncManaged<M> {

    public void withDisplaySync(Consumer<? super D> displaySync);

    public void withDisplaySup(Supplier<? extends D> displaySup);

    public void withDisplayGet(Function<? super D, ? extends M> func);

    public void withDisplaySet(Function<? super M, ? extends D> func);

    public void syncDisplay();

    public void syncManagedFromDisplay();
}
