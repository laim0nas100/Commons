package lt.lb.commons.iteration.general.accessors;

import lt.lb.commons.iteration.general.cons.IterMapCons;
import lt.lb.commons.iteration.general.result.IterMapResult;
import lt.lb.uncheckedutils.SafeOpt;

/**
 *
 * @author laim0nas100
 */
public class MapConsAccessor implements IterMapAccessor {

    @Override
    public <K, V> SafeOpt<IterMapResult<K, V>> tryVisit(int index, K key, V val, IterMapCons<K, V> iter) {
        IterMapResult<K, V> res = new IterMapResult<>(index, key, val);
        return AccessorImpl.visitUncaught(iter, res);
    }

}
