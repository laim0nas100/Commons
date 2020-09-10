package lt.lb.commons.datasync;

/**
 *
 * @author laim0nas100
 */
public interface SyncPersist {

    /**
     * Format the managed value and sync to the persistence gateway
     */
    public void syncPersist();

    /**
     * Get the value form persistence layer, format it and set it to managed
     */
    public void syncManagedFromPersist();
}
