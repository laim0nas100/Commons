package lt.lb.commons.iteration.general.cons;

/**
 *
 * @author laim0nas100
 */
@FunctionalInterface
public interface IterMapBiConsNoStop<K, V> extends IterMapBiCons<K, V> {

    /**
     *
     * @param key
     * @param val
     * @return true = break, false = continue
     */
    @Override
    public default boolean visit(K key, V val) {
        continuedVisit(key, val);
        return false;
    }

    public void continuedVisit(K key, V val);
}
