package lt.lb.commons.iteration.general.accessors.unchecked;

import lt.lb.commons.F;
import lt.lb.commons.iteration.general.accessors.*;
import lt.lb.commons.iteration.general.cons.IterIterableBiCons;
import lt.lb.commons.iteration.general.cons.IterIterableCons;
import lt.lb.commons.iteration.general.result.IterIterableResult;
import lt.lb.uncheckedutils.SafeOpt;

/**
 *
 * @author laim0nas100
 */
public class IterableBiConsAccessorUnchecked extends IterableBiConsAccessor {

    @Override
    public <T> SafeOpt<IterIterableResult<T>> tryVisit(int index, T val, IterIterableCons<T> iter) {
        IterIterableBiCons<T> iterBi = F.cast(iter);
        return AccessorImpl.visitCaught(iterBi, index, val);
    }

}
