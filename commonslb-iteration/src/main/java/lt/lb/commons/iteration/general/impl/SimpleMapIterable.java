package lt.lb.commons.iteration.general.impl;

import java.util.Map;
import lt.lb.uncheckedutils.SafeOpt;
import lt.lb.commons.iteration.general.IterationMap;
import lt.lb.commons.iteration.general.cons.IterMapCons;
import lt.lb.commons.iteration.general.result.IterMapResult;

/**
 *
 * @author laim0nas100
 */
public class SimpleMapIterable extends SimpleAbstractIteration<SimpleMapIterable> implements IterationMap<SimpleMapIterable> {

    @Override
    protected SimpleMapIterable me() {
        return this;
    }

    @Override
    public <K, V> SafeOpt<IterMapResult<K, V>> find(Map<K, V> map, IterMapCons<K, V> iter) {
        return SimpleImpl.find(map, workoutBounds(), onlyIncludingFirst, onlyIncludingLast, resolveAccessor(iter), iter);
    }

}
