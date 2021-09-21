package lt.lb.commons.jpa.ids.idhash;

import lt.lb.commons.containers.collections.WeakConcurrentHashMap;

/**
 *
 * @author laim0nas100
 */
public class SimpleIdHashStore implements IdHashStore {

    protected WeakConcurrentHashMap weakMap = new WeakConcurrentHashMap();

    @Override
    public Object register(Object id, Object localHash) {
        return weakMap.putIfAbsent(id, localHash);
    }

    @Override
    public Object getMapping(Object id) {
        return weakMap.getOrDefault(id, null);
    }

}
