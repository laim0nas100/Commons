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
     * @throws Exception 
     */
    public boolean visit(IterIterableResult<Type> i) throws Exception;

    public static interface IterIterableConsNoStop<Type> extends IterIterableCons<Type> {

        /**
         *
         * @param i
         * @return true = break, false = continue
         * @throws java.lang.Exception
         */
        @Override
        public default boolean visit(IterIterableResult<Type> i) throws Exception {
            continuedVisit(i);
            return false;
        }

        /**
         * 
         * @param i
         * @throws Exception 
         */
        public void continuedVisit(IterIterableResult<Type> i) throws Exception;
    }

}
