package com.github.laim0nas100.commonslb.iteration.general.impl;

import java.util.Map;
import java.util.Optional;
import com.github.laim0nas100.commonslb.iteration.general.IterationMap;
import com.github.laim0nas100.commonslb.iteration.general.cons.IterMapCons;
import com.github.laim0nas100.commonslb.iteration.general.result.IterMapResult;

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
