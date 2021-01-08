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
     * @throws java.lang.Exception
     */
    public boolean visit(Integer index, Type value) throws Exception;

    @Override
    public default boolean visit(IterIterableResult<Type> i) throws Exception {
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
        public default boolean visit(Integer index, Type value) throws Exception {
            continuedVisit(index, value);
            return false;
        }

        public void continuedVisit(Integer index, Type value) throws Exception;
    }

}
