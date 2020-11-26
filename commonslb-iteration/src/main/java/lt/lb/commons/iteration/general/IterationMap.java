package lt.lb.commons.iteration.general;

import java.util.Map;
import java.util.Optional;
import lt.lb.commons.iteration.Iter;
import lt.lb.commons.iteration.general.result.IterMapResult;

/**
 *
 * @author laim0nas100
 */
public interface IterationMap<E extends IterationMap<E>> extends IterationAbstract<E> {

    public <K, V> Optional<IterMapResult<K, V>> find(Map<K, V> map, Iter.IterMap<K, V> iter);

    public default <K, V> void iterate(Map<K, V> map, Iter.IterMapNoStop<K, V> iter) {
        find(map, iter);
    }
}
