package lt.lb.commons.switchmap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import lt.lb.commons.F;
import lt.lb.uncheckedutils.SafeOpt;

/**
 *
 * @author laim0nas100
 */
public abstract class SwitchMapper<T, V, M extends SwitchMapper<T, V, M>> {

    protected final Map<T, Supplier<V>> mapping;
    protected Supplier<V> defaultCase = () -> null;

    public SwitchMapper(Map<T, Supplier<V>> mapping) {
        this.mapping = mapping;
    }

    public abstract M me();

    public Optional<V> toVal(T en) {
        return Optional
                .ofNullable(mapping.getOrDefault(en, null))
                .map(m -> m.get());
    }

    public Optional<V> toMaybe(T en) {
        return SafeOpt.ofNullable(en)
                .map(mapping::get)
                .orGet(() -> defaultCase)
                .map(m -> m.get()).asOptional();
    }

    public Optional<T> toKey(V val) {
        return mapping.entrySet().stream()
                .filter(p -> Objects.equals(val, p.getValue().get()))
                .map(m -> m.getKey()).findFirst();
    }

    public M with(T e, V val) {
        return with(e, () -> val);
    }

    public M withMultipleKeys(V val, T... keys) {
        for (T key : keys) {
            with(key, val);
        }
        return me();
    }

    public M withMultipleKeys(Supplier<V> val, T... keys) {
        for (T key : keys) {
            with(key, val);
        }
        return me();
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

    public List<V> mappedValues(boolean includeDefault) {
        ArrayList<V> list = new ArrayList<>();
        this.mapping.values().forEach(supl -> {
            list.add(supl.get());
        });
        if (includeDefault) {
            list.add(defaultCase.get());
        }

        return list;
    }

    public List<T> mappedKeys() {
        return mapping.keySet().stream().collect(Collectors.toList());
    }

    public static class SimpleSwitchMapper<T, V> extends SwitchMapper<T, V, SimpleSwitchMapper<T, V>> {

        public SimpleSwitchMapper() {
            this(new HashMap<>());
        }

        public SimpleSwitchMapper(Map<T, Supplier<V>> mapping) {
            super(mapping);
        }

        @Override
        public SimpleSwitchMapper<T, V> me() {
            return this;
        }

    }
}
