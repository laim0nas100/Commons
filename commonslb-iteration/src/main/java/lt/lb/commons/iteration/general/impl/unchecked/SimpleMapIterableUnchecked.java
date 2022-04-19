package lt.lb.commons.iteration.general.impl.unchecked;

import java.util.Map;
import lt.lb.commons.iteration.general.IterationMapUnchecked;
import lt.lb.commons.iteration.general.accessors.unchecked.DefaultAccessorResolverUnchecked;
import lt.lb.commons.iteration.general.cons.unchecked.IterMapConsUnchecked;
import lt.lb.commons.iteration.general.impl.*;
import lt.lb.commons.iteration.general.result.IterMapResult;
import lt.lb.uncheckedutils.SafeOpt;

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
