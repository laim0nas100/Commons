package lt.lb.commons.iteration.general.impl;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Objects;
import lt.lb.commons.SafeOpt;
import lt.lb.commons.iteration.general.IterationMap;
import lt.lb.commons.iteration.general.accessors.IterMapAccessor;
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
        Objects.requireNonNull(map, "Map is null");
        Objects.requireNonNull(iter, "Iteration logic is null");
        IterMapAccessor accessor = resolveAccessor(iter);
        IterIterator<Map.Entry<K, V>> bufferedFind = this.bufferedFind(map.entrySet().iterator());
        while (bufferedFind.hasNext()) {
            Map.Entry<K, V> entry = bufferedFind.next();
            SafeOpt<IterMapResult<K, V>> tryVisit = accessor.tryVisit(bufferedFind.getIndex(), entry.getKey(), entry.getValue(), iter);
            if (tryVisit.hasValueOrError()) {
                return tryVisit;
            }
        }
        return SafeOpt.empty();

    }

}
