package lt.lb.commons.iteration.general.cons.unchecked;

import lt.lb.commons.iteration.general.cons.*;
import lt.lb.commons.iteration.general.result.IterIterableResult;
import lt.lb.commons.misc.NestedException;

/**
 *
 * @author laim0nas100
 * @param <Type>
 */
@FunctionalInterface
public interface IterIterableConsUnchecked<Type> extends IterIterableCons<Type> {

    /**
     *
     * @param i
     * @return
     */
    @Override
    public default boolean visit(IterIterableResult<Type> i) {
        try {
            return uncheckedVisit(i);
        } catch (Throwable ex) {
            throw NestedException.of(ex);
        }

    }

    public boolean uncheckedVisit(IterIterableResult<Type> i) throws Throwable;

}
