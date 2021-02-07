package lt.lb.commons.iteration.general.accessors.unchecked;

import lt.lb.commons.SafeOpt;
import lt.lb.commons.iteration.general.accessors.*;
import lt.lb.commons.iteration.general.cons.IterMapCons;
import lt.lb.commons.iteration.general.result.IterMapResult;

/**
 *
 * @author laim0nas100
 */
public class MapConsAccessorUnchecked extends MapConsAccessor {

    @Override
    public <K, V> SafeOpt<IterMapResult<K, V>> tryVisit(int index, K key, V val, IterMapCons<K, V> iter) {
        IterMapResult<K, V> res = new IterMapResult<>(index, key, val);
        try {
            if (iter.visit(res)) {
                return SafeOpt.of(res);
            } else {
                return SafeOpt.empty();
            }
        } catch (Exception ex) {
            return SafeOpt.error(ex);
        }
    }

}
