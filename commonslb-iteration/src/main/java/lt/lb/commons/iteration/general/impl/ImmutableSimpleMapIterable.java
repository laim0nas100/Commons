package lt.lb.commons.iteration.general.impl;

import java.util.Map;
import lt.lb.uncheckedutils.SafeOpt;
import lt.lb.commons.iteration.general.cons.IterMapCons;
import lt.lb.commons.iteration.general.result.IterMapResult;

/**
 *
 * @author laim0nas100
 */
public class ImmutableSimpleMapIterable extends SimpleMapIterable {

    @Override
    protected SimpleMapIterable me() {
        return new SimpleMapIterable();
    }

    @Override
    public <K, V> SafeOpt<IterMapResult<K, V>> find(Map<K, V> map, IterMapCons<K, V> iter) {
        return ImmutableImpl.find(map, resolveAccessor(iter), iter);

    }

}
