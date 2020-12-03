package lt.lb.commons.iteration.general.cons;

import lt.lb.commons.iteration.general.result.IterIterableResult;

/**
 *
 * @author laim0nas100
 */
@FunctionalInterface
public interface IterIterableBiCons<Type> extends IterIterableCons<Type> {

    /**
     *
     * @param index
     * @param value
     * @return true = break, false = continue
     */
    public boolean visit(Integer index, Type value);

    @Override
    public default boolean visit(IterIterableResult<Type> i) {
        return visit(i.index, i.val);
    }

    public static interface IterIterableBiConsNoStop<Type> extends IterIterableBiCons<Type> {

        /**
         *
         * @param index
         * @param value
         * @return true = break, false = continue
         */
        @Override
        public default boolean visit(Integer index, Type value) {
            continuedVisit(index, value);
            return false;
        }

        public void continuedVisit(Integer index, Type value);
    }

}
