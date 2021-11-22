package lt.lb.commons.containers.collections;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author laim0nas100
 *
 * if using Java 9, use respective immutable Map, Set, List, operations instead
 */
public abstract class ImmutableCollections {

    public static final Set UNMODIFIABLE_EMPTY_SET = Collections.unmodifiableSet(Collections.emptySet());
    public static final List UNMODIFIABLE_EMPTY_LIST = Collections.unmodifiableList(Collections.emptyList());
    public static final Map UNMODIFIABLE_EMPTY_MAP = Collections.unmodifiableMap(Collections.emptyMap());

    public static <T> Set<T> setOf(T... items) {
        Set<T> set;
        switch (items.length) {
            case 0:
                return UNMODIFIABLE_EMPTY_SET;
            case 1:
                set = new HashSet<>(1);
                set.add(items[0]);
                break;
            default:
                set = new HashSet<>(items.length);
                set.addAll(Arrays.asList(items));
                break;
        }
        return Collections.unmodifiableSet(set);
    }

    public static <T> List<T> listOf(T... items) {
        if (items.length == 0) {
            return UNMODIFIABLE_EMPTY_LIST;
        } else {
            return Collections.unmodifiableList(Arrays.asList(items));
        }

    }

    public static <K, V> Map<K, V> mapOf(K k0, V v0, K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5) {
        return Collections.unmodifiableMap(
                new MapBuilder<>(new HashMap<K, V>(6))
                        .put(k0, v0)
                        .put(k1, v1)
                        .put(k2, v2)
                        .put(k3, v3)
                        .put(k4, v4)
                        .put(k5, v5)
                        .getMap());
    }

    public static <K, V> Map<K, V> mapOf(K k0, V v0, K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4) {
        return Collections.unmodifiableMap(
                new MapBuilder<>(new HashMap<K, V>(5))
                        .put(k0, v0)
                        .put(k1, v1)
                        .put(k2, v2)
                        .put(k3, v3)
                        .put(k4, v4)
                        .getMap());
    }

    public static <K, V> Map<K, V> mapOf(K k0, V v0, K k1, V v1, K k2, V v2, K k3, V v3) {
        return Collections.unmodifiableMap(
                new MapBuilder<>(new HashMap<K, V>(4))
                        .put(k0, v0)
                        .put(k1, v1)
                        .put(k2, v2)
                        .put(k3, v3)
                        .getMap());
    }

    public static <K, V> Map<K, V> mapOf(K k0, V v0, K k1, V v1, K k2, V v2) {
        return Collections.unmodifiableMap(
                new MapBuilder<>(new HashMap<K, V>(3))
                        .put(k0, v0)
                        .put(k1, v1)
                        .put(k2, v2)
                        .getMap());
    }

    public static <K, V> Map<K, V> mapOf(K k0, V v0, K k1, V v1) {
        return Collections.unmodifiableMap(
                new MapBuilder<>(new HashMap<K, V>(2))
                        .put(k0, v0)
                        .put(k1, v1)
                        .getMap());
    }

    public static <K, V> Map<K, V> mapOf(K k0, V v0) {
        return Collections.unmodifiableMap(Collections.singletonMap(k0, v0));
    }

    public static <K, V> Map<K, V> mapOf() {
        return UNMODIFIABLE_EMPTY_MAP;
    }

}
