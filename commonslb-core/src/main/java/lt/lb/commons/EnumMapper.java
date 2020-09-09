package lt.lb.commons;

import java.util.EnumMap;
import java.util.EnumSet;

/**
 *
 * @author laim0nas100
 */
public abstract class EnumMapper<T extends Enum<T>, V, M extends EnumMapper<T, V, M>> extends SwitchMapper<T, V, M> {
    
    public final Class<T> type;
    
    public EnumMapper(Class<T> type) {
        super(new EnumMap<>(type));
        this.type = type;
    }
    
    public M assertFull() {
        EnumSet<T> allOf = EnumSet.allOf(type);
        for (T en : allOf) {
            if (!mapping.containsKey(en)) {
                throw new IllegalStateException("Missing enum " + en);
            }
        }
        return me();
    }
    
    public M assertFullOrDefault() {
        EnumSet<T> allOf = EnumSet.allOf(type);
        for (T en : allOf) {
            if (!mapping.containsKey(en)) {
                if (defaultCase == null) {
                    throw new IllegalStateException("Missing enum " + en);
                }
            }
        }
        return me();
    }
    
    public M assertFullValues() {
        EnumSet<T> allOf = EnumSet.allOf(type);
        for (T en : allOf) {
            if (!toVal(en).isPresent()) {
                throw new IllegalStateException("Missing enum value for " + en);
            }
        }
        return me();
    }
    
}
