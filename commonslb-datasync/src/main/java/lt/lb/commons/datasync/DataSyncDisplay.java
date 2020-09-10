package lt.lb.commons.datasync;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 *
 * @author laim0nas100
 */
public interface DataSyncDisplay<D, M> extends SyncManaged<M>, SyncDisplay {

    /**
     * Gateway to put data formatted for display layer
     *
     * @param displaySync
     */
    public void withDisplaySync(Consumer<? super D> displaySync);

    /**
     * A gateway to extract data from display layer
     *
     * @param displaySup
     */
    public void withDisplaySup(Supplier<? extends D> displaySup);

    /**
     * Adapter to convert data from display to managed
     *
     * @param func
     */
    public void withDisplayGet(Function<? super D, ? extends M> func);

    /**
     * Adapter to convert data from managed to display layer
     *
     * @param func
     */
    public void withDisplaySet(Function<? super M, ? extends D> func);

}
