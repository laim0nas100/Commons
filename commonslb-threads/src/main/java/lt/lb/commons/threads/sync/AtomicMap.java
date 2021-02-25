package lt.lb.commons.threads.sync;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import lt.lb.commons.containers.values.Value;

/**
 * Atomic change operations in a map. By default uses {@link ConcurrentHashMap}.
 * Every operation is done via {@link Map#compute}, so you must make sure your
 * map is concurrent, or override {@link AtomicMap#atomicChange} with your
 * synchronization.
 *
 * @author laim0nas100
 * @param <K> key type
 * @param <V> value type
 */
public class AtomicMap<K, V> {

    protected Map<K, V> map;
    protected Function<? super K, ? extends V> defaultSupply = k -> null;

    public AtomicMap(Map<K, V> map) {
        this.map = Objects.requireNonNull(map);
    }

    public AtomicMap() {
        this(new ConcurrentHashMap<>());
    }

    /**
     * Sets default value supplier.
     *
     * @param defaultSupply
     */
    public void setDefaultSupply(Function<? super K, ? extends V> defaultSupply) {
        Objects.requireNonNull(defaultSupply);
        this.defaultSupply = defaultSupply;
    }

    /**
     * Gets backing map.
     *
     * @return
     */
    public Map<K, V> getMap() {
        return map;
    }

    /**
     * Gets default value supplier.
     *
     * @return
     */
    public Function<? super K, ? extends V> getDefaultSupply() {
        return defaultSupply;
    }

    /**
     * Delegates to {@link Map#compute }. Subclasses can override this to
     * control concurrency. Every other change method calls this, apart from
     * {@link clear}.
     *
     * @param key
     * @param remappingFunction
     * @return
     */
    public V atomicChange(final K key, final BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        return map.compute(key, remappingFunction);
    }

    /**
     * Atomically changes value of given key based on value remapping function,
     * and makes that value if it was absent. Returns saved value.
     *
     * @param key
     * @param supl
     * @param remappingFunction
     * @return
     */
    public V changeSupplyIfAbsent(final K key, final Function<? super K, ? extends V> supl, final Function<? super V, ? extends V> remappingFunction) {
        Objects.requireNonNull(key);
        Objects.requireNonNull(supl);
        Objects.requireNonNull(remappingFunction);
        return atomicChange(key, (k, v) -> {
            if (v == null) {
                v = supl.apply(key);
            }
            return remappingFunction.apply(v);
        });
    }

    /**
     * Atomically changes value of given key based on value remapping function,
     * and makes that value if it was absent. Returns saved value.
     *
     * @param key
     * @param supl
     * @param remappingFunction
     * @return
     */
    public V changeSupplyIfAbsent(final K key, Supplier<? extends V> supl, final Function<? super V, ? extends V> remappingFunction) {
        Objects.requireNonNull(key);
        Objects.requireNonNull(supl);
        Objects.requireNonNull(remappingFunction);
        return atomicChange(key, (k, v) -> {
            if (v == null) {
                v = supl.get();
            }
            return remappingFunction.apply(v);
        });
    }

    /**
     * Atomically changes value of given key based on value remapping function,
     * and makes that value if it was absent. Returns saved value.
     *
     * @param key
     * @param def
     * @param remappingFunction
     * @return
     */
    public V changeSupplyIfAbsent(final K key, final V def, final Function<? super V, ? extends V> remappingFunction) {
        Objects.requireNonNull(key);
        Objects.requireNonNull(remappingFunction);
        return atomicChange(key, (k, v) -> {
            if (v == null) {
                v = def;
            }
            return remappingFunction.apply(v);
        });
    }

    /**
     * Atomically changes value of given key based on value remapping function,
     * and makes that value if it was absent based on configured default value
     * supplier. Returns saved value.
     *
     * @param key
     * @param remappingFunction
     * @return
     */
    public V changeSupplyIfAbsentDefault(final K key, final Function<? super V, ? extends V> remappingFunction) {
        return changeSupplyIfAbsent(key, defaultSupply, remappingFunction);
    }

    /**
     * Atomically changes value of given key based on value remapping function,
     * if such value is present. Returns saved value.
     *
     * @param key
     * @param remappingFunction
     * @return
     */
    public V changeIfPresent(final K key, final Function<? super V, ? extends V> remappingFunction) {
        Objects.requireNonNull(key);
        Objects.requireNonNull(remappingFunction);
        return atomicChange(key, (k, v) -> {
            if (v == null) {
                return null;
            }
            return remappingFunction.apply(v);
        });
    }

    /**
     * Atomically changes value of given key based on value remapping function,
     * and makes that value if it was absent. Returns saved value.
     *
     * @param key
     * @param supplier
     * @return
     */
    public V changeIfAbsent(final K key, final Supplier<? extends V> supplier) {
        Objects.requireNonNull(key);
        Objects.requireNonNull(supplier);
        return atomicChange(key, (k, v) -> {
            if (v == null) {
                return supplier.get();
            }
            return v;
        });
    }

    /**
     * Atomically remove value of given key. Returns removed value or null.
     *
     * @param key
     * @return
     */
    public V remove(final K key) {
        Objects.requireNonNull(key);
        Value<V> val = new Value<>();
        atomicChange(key, (k, v) -> {
            val.set(v);
            return null;
        });
        return val.get();
    }

    /**
     * Get an snapshot of current keys.
     *
     * @return
     */
    public List<K> getKeys() {
        return new ArrayList<>(map.keySet());
    }

    /**
     * Get an snapshot of current values.
     *
     * @return
     */
    public List<V> getValues() {
        return new ArrayList<>(map.values());
    }

    /**
     * Clears the map.
     */
    public void clear() {
        map.clear();
    }

}
