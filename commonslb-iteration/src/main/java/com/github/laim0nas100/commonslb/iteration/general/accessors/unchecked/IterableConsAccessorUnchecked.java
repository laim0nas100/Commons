package com.github.laim0nas100.commonslb.iteration.general.accessors.unchecked;

import com.github.laim0nas100.uncheckedutils.SafeOpt;
import com.github.laim0nas100.commonslb.iteration.general.accessors.*;
import com.github.laim0nas100.commonslb.iteration.general.cons.IterIterableCons;
import com.github.laim0nas100.commonslb.iteration.general.result.IterIterableResult;

/**
 *
 * @author laim0nas100
 */
public class IterableConsAccessorUnchecked extends IterableConsAccessor {

    @Override
    public <T> SafeOpt<IterIterableResult<T>> tryVisit(int index, T val, IterIterableCons<T> iter) {
        IterIterableResult<T> res = new IterIterableResult<>(index, val);
        return AccessorImpl.visitCaught(iter, res);
    }

}
