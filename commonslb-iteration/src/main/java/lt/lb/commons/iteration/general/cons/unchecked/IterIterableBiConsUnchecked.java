package lt.lb.commons.iteration.general.cons.unchecked;

import lt.lb.commons.iteration.general.cons.*;
import lt.lb.commons.iteration.general.result.IterIterableResult;
import lt.lb.commons.misc.NestedException;

/**
 *
 * @author laim0nas100
 */
@FunctionalInterface
public interface IterIterableBiConsUnchecked<Type> extends IterIterableConsUnchecked<Type>, IterIterableBiCons<Type> {

    /**
     *
     * @param index
     * @param value
     * @return true = break, false = continue
     * @throws java.lang.Exception
     */
    public boolean uncheckedVisit(Integer index, Type value) throws Exception;

    @Override
    public default boolean visit(IterIterableResult<Type> i) {
        return visit(i.index, i.val);
    }

    @Override
    public default boolean uncheckedVisit(IterIterableResult<Type> i) throws Exception {
        return uncheckedVisit(i.index, i.val);
    }

    @Override
    public default boolean visit(Integer index, Type value) {
        try {
            return uncheckedVisit(index, value);
        } catch (Exception ex) {
            throw NestedException.of(ex);
        }
    }

}
