package lt.lb.commons.iteration.general;

import java.util.Map;
import java.util.Optional;
import lt.lb.commons.iteration.general.cons.IterMapBiCons;
import lt.lb.commons.iteration.general.cons.IterMapBiConsNoStop;
import lt.lb.commons.iteration.general.cons.IterMapCons;
import lt.lb.commons.iteration.general.cons.IterMapConsNoStop;
import lt.lb.commons.iteration.general.result.IterMapResult;

/**
 *
 * @author laim0nas100
 */
public interface IterationMap<E extends IterationMap<E>> extends IterationAbstract<E> {

    /**
     * Iterate through map entries
     *
     * @param <K> Key
     * @param <V> Value
     * @param map map instance
     * @param iter iteration logic
     * @return
     */
    public <K, V> Optional<IterMapResult<K, V>> find(Map<K, V> map, IterMapCons<K, V> iter);

    /**
     * Iterate through map entries
     *
     * @param <K> Key
     * @param <V> Value
     * @param map map instance
     * @param iter iteration logic
     * @return
     */
    public default <K, V> Optional<IterMapResult<K, V>> find(Map<K, V> map, IterMapBiCons<K, V> iter) {
        return find(map, (IterMapCons<K, V>) iter);
    }

    public default <K, V> Optional<Void> iterate(Map<K, V> map, IterMapConsNoStop<K, V> iter) {
        return find(map, iter).map(m -> null);
    }

    public default <K, V> Optional<Void> iterate(Map<K, V> map, IterMapBiConsNoStop<K, V> iter) {
        return find(map, iter).map(m -> null);
    }
}
