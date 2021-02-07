package lt.lb.commons.iteration.general.cons;

import lt.lb.commons.iteration.general.result.IterMapResult;

/**
 *
 * @author laim0nas100
 */
@FunctionalInterface
public interface IterMapCons<K, V> {

    public boolean visit(IterMapResult<K, V> entry);

}
