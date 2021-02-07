package lt.lb.commons.iteration.general.cons.unchecked;

import lt.lb.commons.iteration.general.cons.IterMapCons;
import lt.lb.commons.iteration.general.result.IterMapResult;
import lt.lb.commons.misc.NestedException;

/**
 *
 * @author laim0nas100
 */
@FunctionalInterface
public interface IterMapConsUnchecked<K, V> extends IterMapCons<K, V> {

    @Override
    public default boolean visit(IterMapResult<K, V> entry) {
        try {
            return uncheckedVisit(entry);
        } catch (Exception ex) {
            throw NestedException.of(ex);
        }
    }

    public boolean uncheckedVisit(IterMapResult<K, V> entry) throws Exception;

}
