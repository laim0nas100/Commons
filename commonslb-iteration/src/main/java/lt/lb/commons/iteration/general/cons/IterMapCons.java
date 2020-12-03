package lt.lb.commons.iteration.general.cons;

import lt.lb.commons.iteration.general.result.IterMapResult;

/**
 *
 * @author laim0nas100
 */
@FunctionalInterface
public interface IterMapCons<K, V> {

    public boolean visit(IterMapResult<K, V> entry);

    public static interface IterMapConsNoStop<K, V> extends IterMapCons<K, V> {

        /**
         *
         * @return true = break, false = continue
         */
        @Override
        public default boolean visit(IterMapResult<K, V> entry) {
            continuedVisit(entry);
            return false;
        }

        public void continuedVisit(IterMapResult<K, V> entry);
    }
}
