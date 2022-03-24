package lt.lb.commons.jpa.ids;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author laim0nas100
 */
public class GenericIDFactory<I> implements IDFactory<I> {

    protected Map<Class, IdGetter> getters = new ConcurrentHashMap<>();

    @Override
    public <T> IdGetter<T, I> idGetter(Class cls) {
        return getters.computeIfAbsent(cls, k -> IDFactory.super.idGetter(k));
    }

}
