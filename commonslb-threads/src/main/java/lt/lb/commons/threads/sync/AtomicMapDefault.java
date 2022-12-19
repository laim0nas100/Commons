package lt.lb.commons.threads.sync;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 *
 * @author laim0nas100
 * @param <K>
 * @param <V>
 */
public class AtomicMapDefault<K, V> implements AtomicMap<K, V> {

    protected Map<K, V> map;
    protected Function<? super K, ? extends V> defaultSupply = k -> null;

    public AtomicMapDefault(Map<K, V> map) {
        this.map = Objects.requireNonNull(map);
    }

    public AtomicMapDefault() {
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

    @Override
    public Map<K, V> getMap() {
        return map;
    }

    @Override
    public Function<? super K, ? extends V> getDefaultSupply() {
        return defaultSupply;
    }
}
