package lt.lb.commons.datasync;

/**
 *
 * @author laim0nas100
 */
public interface DisplayValidation<M, V extends Valid<M>> extends PureDisplayValidation<M,V> {

    /**
     * Add a display validation strategy
     *
     * @param validation
     */
    public void withDisplayValidation(V validation);


}
