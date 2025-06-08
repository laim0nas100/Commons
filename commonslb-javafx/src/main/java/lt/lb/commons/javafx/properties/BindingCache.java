package lt.lb.commons.javafx.properties;

import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;
import lt.lb.commons.MethodCallSignature;

/**
 *
 * @author laim0nas100
 */
public interface BindingCache {

    public Map<MethodCallSignature, Object> cache();

    public default MethodCallSignature signature(String method, Object... objs) {
        return new MethodCallSignature(method, objs);
    }

    public default <R> R cacheOrGet(String str, Supplier<R> supl) {
        return cacheOrGet(signature(str), supl);
    }

    public default <R> R cacheOrGet(MethodCallSignature str, Supplier<R> supl) {

        Map<MethodCallSignature, Object> cache = cache();

        synchronized (cache) {
            //computeIfAbsent doesn't allow recursion
            if (!cache.containsKey(str)) {
                R supplied = supl.get();
                Objects.requireNonNull(supplied);
                cache.put(str, supplied);
                return supplied;
            } else {
                Object get = cache.get(str);
                if (get != null) {
                    try {
                        return (R) get;
                    } catch (ClassCastException castFail) {
                    }
                }

                R supplied = supl.get();
                Objects.requireNonNull(supplied);
                cache.put(str, supplied);
                return supplied;
            }
        }

    }
}
