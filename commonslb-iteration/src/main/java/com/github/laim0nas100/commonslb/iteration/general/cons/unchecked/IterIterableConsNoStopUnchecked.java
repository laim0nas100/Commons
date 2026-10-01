package com.github.laim0nas100.commonslb.iteration.general.cons.unchecked;

import com.github.laim0nas100.commonslb.iteration.general.result.IterIterableResult;

/**
 *
 * @author laim0nas100
 */
@FunctionalInterface
public interface IterIterableConsNoStopUnchecked<Type> extends IterIterableConsUnchecked<Type> {

    /**
     *
     * @param i
     * @return true = break, false = continue
     * @throws java.lang.Exception
     */
    @Override
    public default boolean uncheckedVisit(IterIterableResult<Type> i) throws Throwable {
        continuedVisit(i);
        return false;
    }

    /**
     *
     * @param i
     * @throws Exception
     */
    public void continuedVisit(IterIterableResult<Type> i) throws Throwable;
}
