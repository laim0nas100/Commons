package com.github.laim0nas100.commonslb.iteration.general.cons.unchecked;

import com.github.laim0nas100.commonslb.iteration.general.cons.IterMapCons;
import com.github.laim0nas100.commonslb.iteration.general.result.IterMapResult;
import com.github.laim0nas100.uncheckedutils.NestedException;

/**
 *
 * @author laim0nas100
 */
@FunctionalInterface
public interface IterMapConsUnchecked<K, V> extends IterMapCons<K, V> {

    @Override
    public default boolean visit(IterMapResult<K, V> entry) {
        try {
            return uncheckedVisit(entry);
        } catch (Throwable ex) {
            throw NestedException.of(ex);
        }
    }

    public boolean uncheckedVisit(IterMapResult<K, V> entry) throws Throwable;

}
