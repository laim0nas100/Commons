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

}
