package lt.lb.commons.iteration.general.accessors.unchecked;

import lt.lb.commons.F;
import lt.lb.commons.SafeOpt;
import lt.lb.commons.iteration.general.accessors.*;
import lt.lb.commons.iteration.general.cons.IterMapBiCons;
import lt.lb.commons.iteration.general.cons.IterMapCons;
import lt.lb.commons.iteration.general.result.IterMapResult;

/**
 *
 * @author laim0nas100
 */
public class MapBiConsAccessorUnchecked extends MapBiConsAccessor {

    @Override
    public <K, V> SafeOpt<IterMapResult<K, V>> tryVisit(int index, K key, V val, IterMapCons<K, V> iter) {
        IterMapBiCons<K, V> iterBi = F.cast(iter);
        try {
            if (iterBi.visit(key, val)) {
                return SafeOpt.of(new IterMapResult<>(index, key, val));
            } else {
                return SafeOpt.empty();
            }
        } catch (Throwable ex) {
            return SafeOpt.error(ex);
        }
    }

}
