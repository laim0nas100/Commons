package lt.lb.commons.iteration.general.impl;

import java.util.Map;
import java.util.Optional;
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
    public <K, V> Optional<IterMapResult<K, V>> find(Map<K, V> map, IterMapCons<K, V> iter) {
        return SimpleImpl.find(map, workoutBounds(), onlyIncludingFirst, onlyIncludingLast, resolveAccessor(iter), iter).asOptional();
    }

}
