package lt.lb.commons.iteration.general.accessors;

import lt.lb.commons.F;
import lt.lb.commons.iteration.general.cons.IterIterableBiCons;
import lt.lb.commons.iteration.general.cons.IterIterableCons;
import lt.lb.commons.iteration.general.result.IterIterableResult;
import lt.lb.uncheckedutils.SafeOpt;

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
