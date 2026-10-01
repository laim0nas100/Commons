package com.github.laim0nas100.commonslb.iteration.general.cons;

import com.github.laim0nas100.commonslb.iteration.general.result.IterMapResult;

/**
 *
 * @author laim0nas100
 */
@FunctionalInterface
public interface IterMapCons<K, V> {

    public boolean visit(IterMapResult<K, V> entry);

}
