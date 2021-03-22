package lt.lb.commons.jpa.searchpart;

import lt.lb.commons.interfaces.CloneSupport;

/**
 *
 * @author laim0nas100
 * @param <M> implementation
 */
public interface SearchPart<M extends SearchPart<M>> extends CloneSupport<M> {

    /**
     * If particular search part is enabled
     *
     * @return
     */
    public boolean isEnabled();

    /**
     * If particular search part is negated
     *
     * @return
     */
    public boolean isNegated();

}
