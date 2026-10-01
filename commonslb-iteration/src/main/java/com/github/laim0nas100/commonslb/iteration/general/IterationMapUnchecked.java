package com.github.laim0nas100.commonslb.iteration.general;

import java.util.Map;
import com.github.laim0nas100.commonslb.iteration.general.cons.unchecked.IterMapBiConsNoStopUnchecked;
import com.github.laim0nas100.commonslb.iteration.general.cons.unchecked.IterMapBiConsUnchecked;
import com.github.laim0nas100.commonslb.iteration.general.cons.unchecked.IterMapConsNoStopUnchecked;
import com.github.laim0nas100.commonslb.iteration.general.cons.unchecked.IterMapConsUnchecked;
import com.github.laim0nas100.commonslb.iteration.general.result.IterMapResult;
import com.github.laim0nas100.uncheckedutils.SafeOpt;

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
