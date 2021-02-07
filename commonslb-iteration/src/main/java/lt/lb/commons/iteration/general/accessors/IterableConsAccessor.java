package lt.lb.commons.iteration.general.accessors;

import lt.lb.commons.SafeOpt;
import lt.lb.commons.iteration.general.cons.IterIterableCons;
import lt.lb.commons.iteration.general.result.IterIterableResult;

/**
 *
 * @author laim0nas100
 */
public class IterableConsAccessor implements IterIterableAccessor {

    @Override
    public <T> SafeOpt<IterIterableResult<T>> tryVisit(int index, T val, IterIterableCons<T> iter) {
        IterIterableResult<T> res = new IterIterableResult<>(index, val);
        if (iter.visit(res)) {
            return SafeOpt.of(res);
        } else {
            return SafeOpt.empty();
        }
    }

}
