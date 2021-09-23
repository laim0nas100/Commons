package lt.lb.commons.jpa.ids.idhash;

import java.util.Map;

/**
 *
 * @author laim0nas100
 */
public interface MapIdHashStore extends IdHashStore {

    public Map getMap();

    @Override
    public default Object register(Object id, Object localHash) {
        Map map = getMap();
        if (map == null) {
            return null;
        }
        return map.putIfAbsent(HashHolder.of(id), HashHolder.of(localHash));
    }

    @Override
    public default Object getMapping(Object id) {
        Map map = getMap();
        if (map == null) {
            return null;
        }
        return map.getOrDefault(HashHolder.of(id), null);
    }

}
