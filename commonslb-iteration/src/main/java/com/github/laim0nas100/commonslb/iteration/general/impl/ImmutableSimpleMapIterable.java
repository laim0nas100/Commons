package com.github.laim0nas100.commonslb.iteration.general.impl;

import java.util.Map;
import java.util.Optional;
import com.github.laim0nas100.commonslb.iteration.general.cons.IterMapCons;
import com.github.laim0nas100.commonslb.iteration.general.result.IterMapResult;

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
    public <K, V> Optional<IterMapResult<K, V>> find(Map<K, V> map, IterMapCons<K, V> iter) {
        return ImmutableImpl.find(map, resolveAccessor(iter), iter).asOptional();

    }

}
