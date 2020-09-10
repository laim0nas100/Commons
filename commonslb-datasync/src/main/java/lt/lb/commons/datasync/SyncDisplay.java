package lt.lb.commons.datasync;

/**
 *
 * @author laim0nas100
 */
public interface SyncDisplay {

    /**
     * Format the managed value and sync to the display gateway
     */
    public void syncDisplay();

    /**
     * Get the value from display layer, format it and set it to managed
     */
    public void syncManagedFromDisplay();
}
