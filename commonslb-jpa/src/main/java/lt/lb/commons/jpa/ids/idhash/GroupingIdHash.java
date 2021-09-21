package lt.lb.commons.jpa.ids.idhash;

import java.util.Map;
import lt.lb.commons.F;
import lt.lb.commons.containers.collections.WeakConcurrentHashMap;

/**
 *
 * @author laim0nas100
 */
public abstract class GroupingIdHash extends SimpleIdHash {

    protected static WeakConcurrentHashMap<Object, IdHashStore> stores = new WeakConcurrentHashMap<>();

    @Override
    public IdHashStore __idHashStore() {
        return __getIdHashStoreMap().computeIfAbsent(F.nullWrap(__groupingProperty(), F.EMPTY_OBJECT), this::__produceIdHashStore);
    }

    protected abstract Object __groupingProperty();

    protected IdHashStore __produceIdHashStore(Object groupingProperty) {
        return new SimpleIdHashStore();
    }

    protected Map<Object, IdHashStore> __getIdHashStoreMap() {
        return stores;
    }

}
