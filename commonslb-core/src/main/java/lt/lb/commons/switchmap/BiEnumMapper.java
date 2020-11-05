package lt.lb.commons.switchmap;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 *
 * @author laim0nas100
 */
public abstract class BiEnumMapper<T extends Enum<T>, V, M extends BiEnumMapper<T, V, M>> extends EnumMapper<T, V, M> {
    
    protected Map<V, T> oppositeMap = new ConcurrentHashMap<>();

    public BiEnumMapper(Class<T> type) {
        super(type);
    }
    
    @Override
    public M with(T e, Supplier<V> val) {
        super.with(e, val);
        V get = val.get();
        if (get != null) {
            oppositeMap.put(get, e);
        }
        
        return me();
    }
    
    @Override
    public M with(T e, V val) {
        super.with(e, val);
        if (val != null) {
            oppositeMap.put(val, e);
        }
        return me();
    }
    
    @Override
    public Optional<T> toKey(V val) {
        return Optional.ofNullable(oppositeMap.computeIfAbsent(val, v -> super.toKey(v).orElse(null)));
    }
    
}
