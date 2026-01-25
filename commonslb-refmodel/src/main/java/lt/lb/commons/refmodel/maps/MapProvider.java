package lt.lb.commons.refmodel.maps;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import lt.lb.commons.F;
import lt.lb.commons.Ins;
import lt.lb.commons.Nulls;
import lt.lb.commons.parsing.numbers.FastParse;
import lt.lb.prebuiltcollections.DelegatingMap;
import lt.lb.uncheckedutils.func.UncheckedSupplier;

/**
 *
 * @author laim0nas100
 */
public interface MapProvider extends DelegatingMap<String, Object> {

    @Override
    public Map<String, Object> delegate();

    public Map<String, Object> createMap();

    public default List createList() {
        return new ArrayList();
    }

    public default void mergeWith(MapProvider other) {
        MapProvider.deepMergeMaps(delegate(), other.delegate());
    }

    public static MapProvider ofMap(Map<String, Object> current, Supplier<? extends Map<String, Object>> supplier) {
        Nulls.requireNonNulls(current, supplier);
        return new MapProvider() {
            @Override
            public Map<String, Object> delegate() {
                return current;
            }

            @Override
            public Map<String, Object> createMap() {
                return supplier.get();
            }

            @Override
            public String toString() {
                return delegate().toString();
            }

            @Override
            public boolean equals(Object o) {
                if (this == o) {
                    return true;
                }
                if (!(o instanceof MapProvider)) {
                    return false;
                }

                MapProvider that = (MapProvider) o;
                return delegate().equals(that.delegate());
            }

            @Override
            public int hashCode() {
                return delegate().hashCode();
            }
        };
    }

    public static MapProvider hashMap(Map<String, Object> current) {
        return ofMap(current, LinkedHashMap::new);
    }

    public static MapProvider ofDefault(Map<String, Object> map) {
        Nulls.requireNonNull(map);
        UncheckedSupplier<Map<String, Object>> supplier = () -> {
            return map.getClass().getDeclaredConstructor().newInstance();
        };
        return ofMap(map, supplier);
    }

    /**
     * Coerces a raw value (usually from Map<String,Object>) to the expected
     * generic type T.
     *
     * @param raw the value read from the map (may be Double, String, Number,
     * null, etc.)
     * @param targetType the expected type
     * @return the coerced value matching type T
     * @throws IllegalArgumentException if conversion is not possible or loses
     * information
     */
    public default <T> T coerceFromRaw(Object raw, Class<T> targetType) {
        Ins<Object> ins = Ins.ofNullablePrimitivePromotion(raw);
        Ins.InsCl<T> target = Ins.of(targetType);
        if (raw == null) {
            return null;
        }

        if (ins.instanceOf(targetType)) {
            return F.cast(raw);
        }

        if (ins.instanceOf(Number.class)) {
            Number n = F.cast(raw);
            if (target.instanceOf(Integer.class)) {
                return F.cast(n.intValue());
            }
            if (target.instanceOf(Long.class)) {
                return F.cast(n.longValue());
            }
            if (target.instanceOf(Float.class)) {
                return F.cast(n.floatValue());
            }
            if (target.instanceOf(Double.class)) {
                return F.cast(n.doubleValue());
            }
            if (target.instanceOf(Short.class)) {
                return F.cast(n.shortValue());
            }
            if (target.instanceOf(Byte.class)) {
                return F.cast(n.byteValue());
            }
            if (target.instanceOf(Boolean.class)) {
                return F.cast(n.intValue() != 0);
            }
        }

        if (ins.instanceOf(String.class)) {
            String str = F.cast(raw);
            str = str.trim();
            if (str.isEmpty()) {
                throw new IllegalArgumentException("Empty string cannot be converted to " + targetType.getName());
            }
            if (target.instanceOf(Integer.class)) {
                return F.cast(FastParse.parseInt(str));
            }
            if (target.instanceOf(Long.class)) {
                return F.cast(FastParse.parseLong(str));
            }
            if (target.instanceOf(Float.class)) {
                return F.cast(FastParse.parseFloat(str));
            }
            if (target.instanceOf(Double.class)) {
                return F.cast(FastParse.parseDouble(str));
            }
            if (target.instanceOf(Short.class)) {
                Integer parseInt = FastParse.parseInt(str);
                if (parseInt != null) {
                    return F.cast(parseInt.shortValue());
                }
                return null;
            }
            if (target.instanceOf(Byte.class)) {
                Integer parseInt = FastParse.parseInt(str);
                if (parseInt != null) {
                    return F.cast(parseInt.byteValue());
                }
                return null;
            }

            if (target.instanceOf(Boolean.class)) {
                String lower = str.trim().toLowerCase();
                if ("true".equals(lower) || "1".equals(lower) || "yes".equals(lower)) {
                    return F.cast(Boolean.TRUE);
                }
                if ("false".equals(lower) || "0".equals(lower) || "no".equals(lower)) {
                    return F.cast(Boolean.FALSE);
                }
            }
        }

        // try simple cast
        try {
            return F.cast(targetType.cast(raw));
        } catch (ClassCastException e) {
            throw new IllegalArgumentException(
                    "Cannot coerce value of type " + raw.getClass().getName()
                    + " to expected type " + targetType.getName()
                    + " (value: " + raw + ")", e);
        }
    }

    @SuppressWarnings("unchecked")
    public static void deepMergeMaps(Map<String, Object> target, Map<String, Object> source) {
        for (Map.Entry<String, Object> entry : source.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            if (!target.containsKey(key)) {
                target.put(key, value);
                continue;
            }

            Object targetVal = target.get(key);

            if (value instanceof Map && targetVal instanceof Map) {
                deepMergeMaps((Map<String, Object>) targetVal, (Map<String, Object>) value);
            } else if (value instanceof List && targetVal instanceof List) {
                ((List<Object>) targetVal).addAll((List<Object>) value);  // append
            } else {
                target.put(key, value);  // replace
            }
        }
    }
}
