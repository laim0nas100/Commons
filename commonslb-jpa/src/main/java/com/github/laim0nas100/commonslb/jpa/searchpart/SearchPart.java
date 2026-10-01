package com.github.laim0nas100.commonslb.jpa.searchpart;

import com.github.laim0nas100.commonslb.clone.CloneSupport;

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
