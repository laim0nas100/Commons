package lt.lb.commons.iteration.general.accessors.unchecked;

import lt.lb.commons.F;
import lt.lb.commons.SafeOpt;
import lt.lb.commons.iteration.general.accessors.*;
import lt.lb.commons.iteration.general.cons.IterIterableBiCons;
import lt.lb.commons.iteration.general.cons.IterIterableCons;
import lt.lb.commons.iteration.general.result.IterIterableResult;

/**
 *
 * @author laim0nas100
 */
public class IterableBiConsAccessorUnchecked extends IterableBiConsAccessor {

    @Override
    public <T> SafeOpt<IterIterableResult<T>> tryVisit(int index, T val, IterIterableCons<T> iter) {
        IterIterableBiCons<T> iterBi = F.cast(iter);
        try {
            if (iterBi.visit(index, val)) {
                return SafeOpt.of(new IterIterableResult<>(index, val));
            } else {
                return SafeOpt.empty();
            }
        } catch (Throwable ex) {
            return SafeOpt.error(ex);
        }
    }

}
