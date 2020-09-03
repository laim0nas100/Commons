package lt.lb.commons.datasync;

import java.util.function.Supplier;

/**
 *
 * @author laim0nas100
 */
public interface SyncAndValidationAggregator extends SyncValidation {

    public default void addSync(DataSyncManaged sync) {
        addSyncPersist(sync);
        addSyncDisplay(sync);
    }
    
    public default void addDataSyncValidation(DataSyncManagedValidation syncValid){
        addSync(syncValid);
        addSyncValidation(syncValid);
    }

    public void addSyncDisplay(DataSyncDisplay sync);

    public void addSyncPersist(DataSyncPersist sync);

    public void addDisplayValidation(DisplayValidation valid);

    public void addPersistValidation(PersistValidation valid);

    public default void addSyncValidation(SyncValidation syncVal) {
        addPersistValidation(syncVal);
        addDisplayValidation(syncVal);
    }

    public <M, V extends Valid<M>> PersistAndDisplayValidation<M, V> createBaseSyncValidationUnmanaged();
    
    public <M, V extends Valid<M>> PersistAndDisplayValidation<M, V> createBaseSyncValidationManaged(Supplier<M> managed);

    public default void addValidationPersist(Valid valid) {
        PersistAndDisplayValidation unmanaged = createBaseSyncValidationUnmanaged();
        unmanaged.withPersistValidation(valid);
        addPersistValidation(unmanaged);
    }

    public default void addValidationDisplay(Valid valid) {
        PersistAndDisplayValidation unmanaged = createBaseSyncValidationUnmanaged();
        unmanaged.withDisplayValidation(valid);
        addDisplayValidation(unmanaged);
    }
    
    public default void addValidationPersist(Supplier supl,Valid valid) {
        PersistAndDisplayValidation managed = createBaseSyncValidationManaged(supl);
        managed.withPersistValidation(valid);
        addPersistValidation(managed);
    }
    
     public default void addValidationDisplay(Supplier supl,Valid valid) {
        PersistAndDisplayValidation managed = createBaseSyncValidationManaged(supl);
        managed.withDisplayValidation(valid);
        addDisplayValidation(managed);
    }
}
