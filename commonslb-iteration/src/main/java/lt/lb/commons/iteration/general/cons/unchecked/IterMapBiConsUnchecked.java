package lt.lb.commons.iteration.general.cons.unchecked;

import lt.lb.commons.iteration.general.cons.IterMapBiCons;
import lt.lb.commons.iteration.general.result.IterMapResult;
import lt.lb.uncheckedutils.NestedException;

/**
 *
 * @author laim0nas100
 */
@FunctionalInterface
public interface IterMapBiConsUnchecked<K, V> extends IterMapConsUnchecked<K, V>, IterMapBiCons<K, V> {

    public boolean uncheckedVisit(K key, V val) throws Throwable;

    @Override
    public default boolean visit(IterMapResult<K, V> entry){
        return visit(entry.key, entry.val);
    }
    
    @Override
    public default boolean uncheckedVisit(IterMapResult<K, V> entry) throws Throwable {
        return uncheckedVisit(entry.key, entry.val);
    }

    @Override
    public default boolean visit(K key, V val) {
        try {
            return uncheckedVisit(key, val);
        } catch (Throwable ex) {
            throw NestedException.of(ex);
        }
    }

}
