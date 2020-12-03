package lt.lb.commons.iteration.general.cons;

import lt.lb.commons.iteration.general.result.IterMapResult;

/**
 *
 * @author laim0nas100
 */
@FunctionalInterface
public interface IterMapBiCons<K, V> extends IterMapCons<K, V> {

    public boolean visit(K key, V val);

    @Override
    public default boolean visit(IterMapResult<K, V> entry) {
        return visit(entry.key, entry.val);
    }

    public static interface IterMapBiConsNoStop<K, V> extends IterMapBiCons<K, V> {

        /**
         *
         * @return true = break, false = continue
         */
        @Override
        public default boolean visit(K key, V val) {
            continuedVisit(key, val);
            return false;
        }

        public void continuedVisit(K key, V val);
    }
}
