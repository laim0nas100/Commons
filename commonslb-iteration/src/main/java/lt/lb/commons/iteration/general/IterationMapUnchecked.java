package lt.lb.commons.iteration.general;

import java.util.Map;
import lt.lb.commons.SafeOpt;
import lt.lb.commons.iteration.general.cons.unchecked.IterMapBiConsNoStopUnchecked;
import lt.lb.commons.iteration.general.cons.unchecked.IterMapBiConsUnchecked;
import lt.lb.commons.iteration.general.cons.unchecked.IterMapConsNoStopUnchecked;
import lt.lb.commons.iteration.general.cons.unchecked.IterMapConsUnchecked;
import lt.lb.commons.iteration.general.result.IterMapResult;

/**
 *
 * @author laim0nas100
 */
public interface IterationMapUnchecked<E extends IterationMapUnchecked<E>> extends IterationAbstract<E> {

    /**
     * Iterate through map entries
     *
     * @param <K> Key
     * @param <V> Value
     * @param map map instance
     * @param iter iteration logic
     * @return
     */
    public <K, V> SafeOpt<IterMapResult<K, V>> find(Map<K, V> map, IterMapConsUnchecked<K, V> iter);

    /**
     * Iterate through map entries
     *
     * @param <K> Key
     * @param <V> Value
     * @param map map instance
     * @param iter iteration logic
     * @return
     */
    public default <K, V> SafeOpt<IterMapResult<K, V>> find(Map<K, V> map, IterMapBiConsUnchecked<K, V> iter) {
        return find(map, (IterMapConsUnchecked<K, V>) iter);
    }

    public default <K, V> SafeOpt<Void> iterate(Map<K, V> map, IterMapConsNoStopUnchecked<K, V> iter) {
        return find(map, iter).keepError();
    }

    public default <K, V> SafeOpt<Void> iterate(Map<K, V> map, IterMapBiConsNoStopUnchecked<K, V> iter) {
        return find(map, iter).keepError();
    }
}
