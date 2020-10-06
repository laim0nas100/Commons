/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package lt.lb.commons.switchmap;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

/**
 *
 * @author laim0nas100
 */
public abstract class BiEnumMapper <T extends Enum<T>, V, M extends BiEnumMapper<T,V,M>> extends EnumMapper<T,V,M>{

    
    protected Map<V,T> oppositeMap = new HashMap<>();
    public BiEnumMapper(Class<T> type) {
        super(type);
    }

    @Override
    public M with(T e, Supplier<V> val) {
        super.with(e, val);
        oppositeMap.put(val.get(), e);
        return me();
    }

    @Override
    public M with(T e, V val) {
        super.with(e, val);
        oppositeMap.put(val, e);
        return me();
    }

    @Override
    public Optional<T> toKey(V val) {
        return Optional.ofNullable(oppositeMap.getOrDefault(val, null));
    }
    
    
    
    

    
}
