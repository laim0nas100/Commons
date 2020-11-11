package lt.lb.commons.datasync;

/**
 *
 * @author laim0nas100
 */
public interface PurePersistValidation<M, V extends Valid<M>> {

    /**
     * If managed value can be persisted
     *
     * @return
     */
    public boolean validPersist();

    /**
     * If managed value can be persisted, fire every validation
     *
     * @return
     */
    public boolean validPersistFull();

     /**
     * If managed value can NOT be persisted, fire FIRST validation
     *
     * @return
     */
    public default boolean invalidPersist() {
        return !validPersist();
    }

    /**
     * If managed value can NOT be persisted, fire EVERY validation
     *
     * @return
     */
    public default boolean invalidPersistFull() {
        return !validPersistFull();
    }

    /**
     * Check only conditions, don't actually show anything
     *
     * @param from
     * @return
     */
    public boolean isValidPersist(M from);

    /**
     * Check only conditions, don't actually show anything
     *
     * @param from
     * @return
     */
    public default boolean isInvalidPersist(M from) {
        return !isValidPersist(from);
    }

    /**
     * Clear validation
     *
     * @param from
     */
    public void clearInvalidationPersist(M from);
}
