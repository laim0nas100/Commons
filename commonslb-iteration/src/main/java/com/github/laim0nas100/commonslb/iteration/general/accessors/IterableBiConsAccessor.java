package com.github.laim0nas100.commonslb.iteration.general.accessors;

import com.github.laim0nas100.commonslb.F;
import com.github.laim0nas100.commonslb.iteration.general.cons.IterIterableBiCons;
import com.github.laim0nas100.commonslb.iteration.general.cons.IterIterableCons;
import com.github.laim0nas100.commonslb.iteration.general.result.IterIterableResult;
import com.github.laim0nas100.uncheckedutils.SafeOpt;

/**
 *
 * @author laim0nas100
 */
public class IterableBiConsAccessor implements IterIterableAccessor {

    @Override
    public <T> SafeOpt<IterIterableResult<T>> tryVisit(int index, T val, IterIterableCons<T> iter) {
        IterIterableBiCons<T> iterBi = F.cast(iter);
        return AccessorImpl.visitUncaught(iterBi, index, val);
    }

}
