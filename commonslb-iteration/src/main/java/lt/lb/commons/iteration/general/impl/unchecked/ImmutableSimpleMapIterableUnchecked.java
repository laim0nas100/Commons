package lt.lb.commons.iteration.general.impl.unchecked;

import java.util.Map;
import lt.lb.commons.SafeOpt;
import lt.lb.commons.iteration.general.cons.unchecked.IterMapBiConsUnchecked;
import lt.lb.commons.iteration.general.impl.ImmutableImpl;
import lt.lb.commons.iteration.general.result.IterMapResult;

/**
 *
 * @author laim0nas100
 */
public class ImmutableSimpleMapIterableUnchecked extends SimpleMapIterableUnchecked {

    @Override
    protected SimpleMapIterableUnchecked me() {
        return new SimpleMapIterableUnchecked();
    }

    @Override
    public <K, V> SafeOpt<IterMapResult<K, V>> find(Map<K, V> map, IterMapBiConsUnchecked<K, V> iter) {
        return ImmutableImpl.find(map, resolveAccessor(iter), iter);
    }

}
