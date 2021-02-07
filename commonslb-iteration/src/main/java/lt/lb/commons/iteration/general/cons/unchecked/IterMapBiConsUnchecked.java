package lt.lb.commons.iteration.general.cons.unchecked;

import lt.lb.commons.iteration.general.cons.IterMapBiCons;
import lt.lb.commons.iteration.general.result.IterMapResult;
import lt.lb.commons.misc.NestedException;

/**
 *
 * @author laim0nas100
 */
@FunctionalInterface
public interface IterMapBiConsUnchecked<K, V> extends IterMapConsUnchecked<K, V>, IterMapBiCons<K, V> {

    public boolean uncheckedVisit(K key, V val) throws Exception;

    @Override
    public default boolean visit(IterMapResult<K, V> entry){
        return visit(entry.key, entry.val);
    }
    
    @Override
    public default boolean uncheckedVisit(IterMapResult<K, V> entry) throws Exception {
        return uncheckedVisit(entry.key, entry.val);
    }

    @Override
    public default boolean visit(K key, V val) {
        try {
            return uncheckedVisit(key, val);
        } catch (Exception ex) {
            throw NestedException.of(ex);
        }
    }

}
