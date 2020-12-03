package lt.lb.commons.iteration.general.cons;

import lt.lb.commons.iteration.general.result.IterIterableResult;

/**
 *
 * @author laim0nas100
 */
@FunctionalInterface
public interface IterIterableCons<Type> {

    
    public boolean visit(IterIterableResult<Type> i);
    
    
    public static interface IterIterableConsNoStop<Type> extends IterIterableCons<Type> {

        /**
         *
         * @return true = break, false = continue
         */
        public default boolean visit(IterIterableResult<Type> i) {
            continuedVisit(i);
            return false;
        }
        
        public void continuedVisit(IterIterableResult<Type> i);
    }
    
}
