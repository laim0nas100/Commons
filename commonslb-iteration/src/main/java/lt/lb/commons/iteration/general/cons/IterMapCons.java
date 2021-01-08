package lt.lb.commons.iteration.general.cons;

import lt.lb.commons.iteration.general.result.IterMapResult;

/**
 *
 * @author laim0nas100
 */
@FunctionalInterface
public interface IterMapCons<K, V> {

    public boolean visit(IterMapResult<K, V> entry) throws Exception;

    public static interface IterMapConsNoStop<K, V> extends IterMapCons<K, V> {

        /**
         *
         * @param entry
         * @return true = break, false = continue
         * @throws java.lang.Exception
         */
        @Override
        public default boolean visit(IterMapResult<K, V> entry) throws Exception {
            continuedVisit(entry);
            return false;
        }

        public void continuedVisit(IterMapResult<K, V> entry) throws Exception;
    }
}
