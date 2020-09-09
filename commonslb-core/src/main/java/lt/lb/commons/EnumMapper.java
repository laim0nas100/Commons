package lt.lb.commons;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

/**
 *
 * @author laim0nas100
 */
public abstract class EnumMapper<T extends Enum<T>, V, M extends EnumMapper<T, V, M>> {

    public final Class<T> type;
    protected final EnumMap<T, Supplier<V>> mapping;
    protected Supplier<V> defaultCase;

    public EnumMapper(Class<T> type) {
        this.type = type;
        this.mapping = new EnumMap<>(type);
    }

    public abstract M me();

    public Optional<V> toVal(T en) {
        return Optional.ofNullable(mapping.getOrDefault(en, null)).map(m -> m.get());
    }

    public Optional<V> toMaybe(T en) {
        return Optional.ofNullable(mapping.getOrDefault(en, defaultCase)).map(m -> m.get());
    }

    public Optional<T> toEnum(V val) {
        return F.find(mapping, (key, v) -> {
            return Objects.equals(v.get(), val);
        }).map(m -> m.getG1());
    }

    public M with(T e, V val) {
        return with(e, () -> val);
    }

    public M with(T e, Supplier<V> val) {
        M me = me();
        me.mapping.put(e, val);
        return me;
    }

    public M withDefaultCase(Supplier<V> val) {
        M me = me();
        me.defaultCase = val;
        return me;
    }

    public M withDefaultCase(V val) {
        return withDefaultCase(() -> val);
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
