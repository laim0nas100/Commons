package lt.lb.commons.iteration.general.accessors;

import lt.lb.commons.F;
import lt.lb.commons.iteration.general.cons.IterMapBiCons;
import lt.lb.commons.iteration.general.cons.IterMapCons;
import lt.lb.commons.iteration.general.result.IterMapResult;
import lt.lb.uncheckedutils.SafeOpt;

/**
 *
 * @author laim0nas100
 */
public class MapBiConsAccessor implements IterMapAccessor {

    @Override
    public <K, V> SafeOpt<IterMapResult<K, V>> tryVisit(int index, K key, V val, IterMapCons<K, V> iter) {
        IterMapBiCons<K, V> iterBi = F.cast(iter);
        return AccessorImpl.visitUncaught(index, key, val, iterBi);
    }

}
