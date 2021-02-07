package lt.lb.commons.iteration.general.cons.unchecked;

/**
 *
 * @author laim0nas100
 */
@FunctionalInterface
public interface IterMapBiConsNoStopUnchecked<K, V> extends IterMapBiConsUnchecked<K, V> {

    /**
     *
     * @param key
     * @param val
     * @return true = break, false = continue
     * @throws java.lang.Exception
     */
    @Override
    public default boolean uncheckedVisit(K key, V val) throws Exception {
        continuedVisit(key, val);
        return false;
    }

    public void continuedVisit(K key, V val) throws Exception;
}
