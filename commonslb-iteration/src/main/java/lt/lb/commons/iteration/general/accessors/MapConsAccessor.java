package lt.lb.commons.iteration.general.accessors;

import lt.lb.commons.SafeOpt;
import lt.lb.commons.iteration.general.cons.IterMapCons;
import lt.lb.commons.iteration.general.result.IterMapResult;

/**
 *
 * @author laim0nas100
 */
public class MapConsAccessor implements IterMapAccessor {

    @Override
    public <K, V> SafeOpt<IterMapResult<K, V>> tryVisit(int index, K key, V val, IterMapCons<K, V> iter) {
        IterMapResult<K, V> res = new IterMapResult<>(index, key, val);
        if (iter.visit(res)) {
            return SafeOpt.of(res);
        } else {
            return SafeOpt.empty();
        }
    }

}
