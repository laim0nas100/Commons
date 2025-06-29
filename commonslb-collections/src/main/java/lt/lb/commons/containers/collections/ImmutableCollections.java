package lt.lb.commons.containers.collections;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
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
        switch (items.length) {
            case 0:
                return UNMODIFIABLE_EMPTY_SET;
            case 1:
                return Collections.singleton(items[0]);
            default:
                if(items.length <= 16){
                    return new ImmutableLinearSet<>(items);
                }
                LinkedHashSet<T> set = new LinkedHashSet<>(items.length, 1f);
                for(T item:items){
                    boolean add = set.add(item);
                    if(!add){
                        throw new IllegalArgumentException("Duplicate element in a set:"+item);
                    }
                }
                return Collections.unmodifiableSet(set);
        }
    }

    public static <T> List<T> listOf(T... items) {
        if (items.length == 0) {
            return UNMODIFIABLE_EMPTY_LIST;
        } else {
            return Collections.unmodifiableList(Arrays.asList(items));
        }
    }

    public static <K, V> Map<K, V> mapOf(
            K k0, V v0,
            K k1, V v1,
            K k2, V v2,
            K k3, V v3,
            K k4, V v4,
            K k5, V v5,
            K k6, V v6,
            K k7, V v7,
            K k8, V v8,
            K k9, V v9
    ) {
        return new ImmutableLinearMap(k0, v0, k1, v1, k2, v2, k3, v3, k4, v4, k5, v5, k6, v6, k7, v7, k8, v8, k9, v9);
    }

    public static <K, V> Map<K, V> mapOf(
            K k0, V v0,
            K k1, V v1,
            K k2, V v2,
            K k3, V v3,
            K k4, V v4,
            K k5, V v5,
            K k6, V v6,
            K k7, V v7,
            K k8, V v8
    ) {
        return new ImmutableLinearMap(k0, v0, k1, v1, k2, v2, k3, v3, k4, v4, k5, v5, k6, v6, k7, v7, k8, v8);

    }

    public static <K, V> Map<K, V> mapOf(
            K k0, V v0,
            K k1, V v1,
            K k2, V v2,
            K k3, V v3,
            K k4, V v4,
            K k5, V v5,
            K k6, V v6,
            K k7, V v7
    ) {
        return new ImmutableLinearMap(k0, v0, k1, v1, k2, v2, k3, v3, k4, v4, k5, v5, k6, v6, k7, v7);
    }

    public static <K, V> Map<K, V> mapOf(
            K k0, V v0,
            K k1, V v1,
            K k2, V v2,
            K k3, V v3,
            K k4, V v4,
            K k5, V v5,
            K k6, V v6
    ) {
        return new ImmutableLinearMap(k0, v0, k1, v1, k2, v2, k3, v3, k4, v4, k5, v5, k6, v6);
    }

    public static <K, V> Map<K, V> mapOf(
            K k0, V v0,
            K k1, V v1,
            K k2, V v2,
            K k3, V v3,
            K k4, V v4,
            K k5, V v5
    ) {
        return new ImmutableLinearMap(k0, v0, k1, v1, k2, v2, k3, v3, k4, v4, k5, v5);
    }

    public static <K, V> Map<K, V> mapOf(
            K k0, V v0,
            K k1, V v1,
            K k2, V v2,
            K k3, V v3,
            K k4, V v4
    ) {
        return new ImmutableLinearMap(k0, v0, k1, v1, k2, v2, k3, v3, k4, v4);
    }

    public static <K, V> Map<K, V> mapOf(
            K k0, V v0,
            K k1, V v1,
            K k2, V v2,
            K k3, V v3
    ) {
        return new ImmutableLinearMap(k0, v0, k1, v1, k2, v2, k3, v3);
    }

    public static <K, V> Map<K, V> mapOf(K k0, V v0, K k1, V v1, K k2, V v2) {
        return new ImmutableLinearMap(k0, v0, k1, v1, k2, v2);
    }

    public static <K, V> Map<K, V> mapOf(K k0, V v0, K k1, V v1) {
        return new ImmutableLinearMap(k0, v0, k1, v1);
    }

    public static <K, V> Map<K, V> mapOf(K k0, V v0) {
        return new ImmutableLinearMap(k0, v0);
    }

    public static <K, V> Map<K, V> mapOf() {
        return UNMODIFIABLE_EMPTY_MAP;
    }

}
