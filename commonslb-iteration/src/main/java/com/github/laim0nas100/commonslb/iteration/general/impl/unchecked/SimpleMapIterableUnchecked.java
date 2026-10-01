package com.github.laim0nas100.commonslb.iteration.general.impl.unchecked;

import java.util.Map;
import com.github.laim0nas100.commonslb.iteration.general.IterationMapUnchecked;
import com.github.laim0nas100.commonslb.iteration.general.accessors.unchecked.DefaultAccessorResolverUnchecked;
import com.github.laim0nas100.commonslb.iteration.general.cons.unchecked.IterMapConsUnchecked;
import com.github.laim0nas100.commonslb.iteration.general.impl.*;
import com.github.laim0nas100.commonslb.iteration.general.result.IterMapResult;
import com.github.laim0nas100.uncheckedutils.SafeOpt;

/**
 *
 * @author laim0nas100
 */
public class SimpleMapIterableUnchecked extends SimpleAbstractIteration<SimpleMapIterableUnchecked> implements IterationMapUnchecked<SimpleMapIterableUnchecked> {

    public SimpleMapIterableUnchecked() {
        this.accessorResolver = new DefaultAccessorResolverUnchecked();
    }

    @Override
    protected SimpleMapIterableUnchecked me() {
        return this;
    }

    @Override
    public <K, V> SafeOpt<IterMapResult<K, V>> find(Map<K, V> map, IterMapConsUnchecked<K, V> iter) {
        return SimpleImpl.find(map, workoutBounds(), onlyIncludingFirst, onlyIncludingLast, resolveAccessor(iter), iter);
    }

}
