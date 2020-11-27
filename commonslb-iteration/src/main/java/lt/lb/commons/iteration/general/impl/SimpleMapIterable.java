package lt.lb.commons.iteration.general.impl;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;
import lt.lb.commons.iteration.Iter;
import lt.lb.commons.iteration.general.IterationMap;
import lt.lb.commons.iteration.general.result.IterMapResult;

/**
 *
 * @author laim0nas100
 */
public class SimpleMapIterable extends SimpleAbstractIteration<SimpleMapIterable> implements IterationMap<SimpleMapIterable> {

    @Override
    public <K, V> Optional<IterMapResult<K, V>> find(Map<K, V> map, Iter.IterMap<K, V> iter) {
        Iterator<Map.Entry<K, V>> iterator = map.entrySet().iterator();

        if (onlyIncludingFirst == 0 || onlyIncludingLast == 0) {
            return Optional.empty();
        }
        if (onlyIncludingFirst > 0 && onlyIncludingLast > 0) {
            throw new IllegalArgumentException("Can't include only first AND only last, please pick one or the other");
        }

        int lastSize = 0;
        LinkedList<Map.Entry<K, V>> lastBuffer = null;
        if (onlyIncludingLast > 0) {
            lastBuffer = new LinkedList<>();
            boolean reachedEnd = false;
            while (iterator.hasNext() && !reachedEnd) {

                Map.Entry<K, V> entry = iterator.next();

                if (lastSize >= onlyIncludingLast) {
                    lastBuffer.addLast(entry);
                    lastBuffer.removeFirst();
                } else {
                    lastBuffer.addLast(entry);
                    lastSize++;
                }

            }
            // just iterate through the last elements
            for (Map.Entry<K, V> ent : lastBuffer) {
                if (iter.visit(ent.getKey(), ent.getValue())) {
                    return Optional.of(new IterMapResult<>(ent.getKey(), ent.getValue()));
                }
            }

        } else {
            int firstToInclude = onlyIncludingFirst;
            while (iterator.hasNext()) {
                Map.Entry<K, V> entry = iterator.next();
                if (onlyIncludingFirst > 0) {
                    if (firstToInclude > 0) {
                        firstToInclude--;
                    } else {
                        return Optional.empty();
                    }
                }
                K key = entry.getKey();
                V val = entry.getValue();
                if (iter.visit(key, val)) {
                    return Optional.of(new IterMapResult<>(key, val));
                }
            }
        }

        return Optional.empty();
    }

    @Override
    protected SimpleMapIterable me() {
        return this;
    }

}
