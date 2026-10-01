package com.github.laim0nas100.commonslb.iteration.general;

import java.util.Map;
import java.util.Optional;
import com.github.laim0nas100.commonslb.iteration.general.cons.IterMapBiCons;
import com.github.laim0nas100.commonslb.iteration.general.cons.IterMapBiConsNoStop;
import com.github.laim0nas100.commonslb.iteration.general.cons.IterMapCons;
import com.github.laim0nas100.commonslb.iteration.general.cons.IterMapConsNoStop;
import com.github.laim0nas100.commonslb.iteration.general.result.IterMapResult;

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
