package lt.lb.commons.iteration.general.cons;

import lt.lb.commons.iteration.general.result.IterIterableResult;

/**
 *
 * @author laim0nas100
 */
@FunctionalInterface
public interface IterIterableConsNoStop<Type> extends IterIterableCons<Type> {

    /**
     *
     * @param i
     * @return true = break, false = continue
     */
    @Override
    public default boolean visit(IterIterableResult<Type> i) {
        continuedVisit(i);
        return false;
    }

    /**
     *
     * @param i
     */
    public void continuedVisit(IterIterableResult<Type> i);
}
