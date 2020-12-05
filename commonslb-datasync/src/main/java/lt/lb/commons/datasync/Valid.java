package lt.lb.commons.datasync;

/**
 *
 * @author laim0nas100
 */
public interface Valid<M> {

    /**
     * Test if condition is valid with given parameter, which can be empty,
     * depends on the implementation. Does not invoke a validation message.
     *
     * @param from
     * @return
     */
    public boolean isValid(M from);

    /**
     * Analogous to isValid, but negated
     *
     * @param from
     * @return
     */
    public default boolean isInvalid(M from) {
        return !isValid(from);
    }

    /**
     * Show validation with given parameter. Message can change depending on the
     * parameter.
     *
     * @param from
     */
    public void showInvalidation(M from);

    /**
     * Clear validation, depending on the parameter. Usually the parameter is
     * ignored, but can depend on the implementation.
     *
     * @param from
     */
    public void clearInvalidation(M from);
}
