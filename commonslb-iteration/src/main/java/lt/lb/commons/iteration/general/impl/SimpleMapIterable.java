package lt.lb.commons.iteration.general.impl;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import lt.lb.commons.SafeOpt;
import lt.lb.commons.iteration.general.IterationMap;
import lt.lb.commons.iteration.general.cons.IterMapCons;
import lt.lb.commons.iteration.general.result.IterMapResult;

/**
 *
 * @author laim0nas100
 */
public class SimpleMapIterable extends SimpleAbstractIteration<SimpleMapIterable> implements IterationMap<SimpleMapIterable> {

    @Override
    public <K, V> SafeOpt<IterMapResult<K, V>> find(Map<K, V> map, IterMapCons<K, V> iter) {
        Iterator<Map.Entry<K, V>> iterator = map.entrySet().iterator();

        if (onlyIncludingFirst == 0 || onlyIncludingLast == 0) {
            return SafeOpt.empty();
        }
        if (onlyIncludingFirst > 0 && onlyIncludingLast > 0) {
            throw new IllegalArgumentException("Can't include only first AND only last, please pick one or the other");
        }

        int lastSize = 0;
        int index = -1;

        IterMapAccessor accessor = resolveAccessor(iter);
        LinkedList<Map.Entry<K, V>> lastBuffer = null;
        if (onlyIncludingLast > 0) {
            lastBuffer = new LinkedList<>();
            boolean reachedEnd = false;
            while (iterator.hasNext() && !reachedEnd) {

                index++;
                Map.Entry<K, V> entry = iterator.next();

                if (lastSize >= onlyIncludingLast) {
                    lastBuffer.addLast(entry);
                    lastBuffer.removeFirst();
                } else {
                    lastBuffer.addLast(entry);
                    lastSize++;
                }

            }
            int lastIndex = index - lastSize;
            // just iterate through the last elements
            for (Map.Entry<K, V> ent : lastBuffer) {
                SafeOpt<IterMapResult<K, V>> tryVisit = accessor.tryVisit(lastIndex, ent.getKey(), ent.getValue(), iter);
                if (tryVisit.hasValueOrError()) {
                    return tryVisit;
                }
                lastIndex++;
            }

        } else {
            int firstToInclude = onlyIncludingFirst;
            while (iterator.hasNext()) {
                index++;
                Map.Entry<K, V> entry = iterator.next();
                if (onlyIncludingFirst > 0) {
                    if (firstToInclude > 0) {
                        firstToInclude--;
                    } else {
                        return SafeOpt.empty();
                    }
                }
                SafeOpt<IterMapResult<K, V>> tryVisit = accessor.tryVisit(index, entry.getKey(), entry.getValue(), iter);
                if (tryVisit.hasValueOrError()) {
                    return tryVisit;
                }
            }
        }

        return SafeOpt.empty();
    }

    @Override
    protected SimpleMapIterable me() {
        return this;
    }

}
