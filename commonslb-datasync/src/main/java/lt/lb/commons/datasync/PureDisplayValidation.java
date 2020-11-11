package lt.lb.commons.datasync;

/**
 *
 * @author laim0nas100
 */
public interface PureDisplayValidation<M, V extends Valid<M>> {

    /**
     * If managed value can be displayed, don't fire validation
     *
     * @return
     */
    public boolean validDisplay();

    /**
     * If managed value can be displayed, fire EVERY validation
     *
     * @return
     */
    public boolean validDisplayFull();

    /**
     * If managed value can NOT be displayed, fire FIRST validation
     *
     * @return
     */
    public default boolean invalidDisplay() {
        return !validDisplay();
    }

    /**
     * If managed value can NOT be displayed, fire EVERY validation
     *
     * @return
     */
    public default boolean invalidDisplayFull() {
        return !validDisplayFull();
    }

    /**
     * Check only conditions, don't actually show anything
     *
     * @param from
     * @return
     */
    public boolean isValidDisplay(M from);

    /**
     * Check only conditions, don't actually show anything
     *
     * @param from
     * @return
     */
    public default boolean isInvalidDisplay(M from) {
        return !isValidDisplay(from);
    }

    /**
     * Clear validation
     *
     * @param from
     */
    public void clearInvalidationDisplay(M from);
}
