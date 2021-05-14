package lt.lb.commons.iteration.general.accessors.unchecked;

import lt.lb.uncheckedutils.SafeOpt;
import lt.lb.commons.iteration.general.accessors.*;
import lt.lb.commons.iteration.general.cons.IterIterableCons;
import lt.lb.commons.iteration.general.result.IterIterableResult;

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
