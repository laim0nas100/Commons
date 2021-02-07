package lt.lb.commons.iteration.general.cons;

import lt.lb.commons.iteration.general.result.IterIterableResult;

/**
 *
 * @author laim0nas100
 * @param <Type>
 */
@FunctionalInterface
public interface IterIterableCons<Type> {

    /**
     *
     * @param i
     * @return
     */
    public boolean visit(IterIterableResult<Type> i);
}
