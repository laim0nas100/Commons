package lt.lb.commons.iteration.general.accessors.unchecked;

import lt.lb.commons.iteration.general.accessors.*;
import lt.lb.commons.iteration.general.cons.IterMapCons;
import lt.lb.commons.iteration.general.result.IterMapResult;
import com.github.laim0nas100.uncheckedutils.NestedException;
import com.github.laim0nas100.uncheckedutils.SafeOpt;

/**
 *
 * @author laim0nas100
 */
public class MapConsAccessorUnchecked extends MapConsAccessor {

    @Override
    public <K, V> SafeOpt<IterMapResult<K, V>> tryVisit(int index, K key, V val, IterMapCons<K, V> iter) {
        IterMapResult<K, V> res = new IterMapResult<>(index, key, val);
        return AccessorImpl.visitCaught(iter, res);
    }

}
