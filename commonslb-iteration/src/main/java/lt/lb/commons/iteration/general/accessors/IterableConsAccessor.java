package lt.lb.commons.iteration.general.accessors;

import lt.lb.commons.iteration.general.cons.IterIterableCons;
import lt.lb.commons.iteration.general.result.IterIterableResult;
import lt.lb.uncheckedutils.SafeOpt;

/**
 *
 * @author laim0nas100
 */
public class IterableConsAccessor implements IterIterableAccessor {

    @Override
    public <T> SafeOpt<IterIterableResult<T>> tryVisit(int index, T val, IterIterableCons<T> iter) {
        IterIterableResult<T> res = new IterIterableResult<>(index, val);
        return AccessorImpl.visitUncaught(iter,res);
    }

}
