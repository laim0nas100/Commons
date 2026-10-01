package com.github.laim0nas100.commonslb.iteration.general.cons;

import com.github.laim0nas100.commonslb.iteration.general.result.IterIterableResult;

/**
 *
 * @author laim0nas100
 * @param <Type>
 */
@FunctionalInterface
public interface IterIterableCons<Type> {

    /**
     *
     * @param i
     * @return
     */
    public boolean visit(IterIterableResult<Type> i);
}
