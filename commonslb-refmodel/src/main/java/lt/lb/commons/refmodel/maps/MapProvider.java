/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package lt.lb.commons.refmodel.maps;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;
import lt.lb.commons.F;
import lt.lb.commons.Nulls;
import lt.lb.uncheckedutils.func.UncheckedSupplier;

/**
 *
 * @author laim0nas100
 */
public interface MapProvider {

    public Map<String, Object> getMap();

    public Map<String, Object> createMap();

    public default List createList() {
        return new ArrayList();
    }

    public static MapProvider ofMap(Map<String, Object> current, Supplier<? extends Map<String, Object>> supplier) {
        Nulls.requireNonNulls(current, supplier);
        return new MapProvider() {
            @Override
            public Map<String, Object> getMap() {
                return current;
            }

            @Override
            public Map<String, Object> createMap() {
                return supplier.get();
            }
        };
    }

    public static MapProvider hashMap(Map<String, Object> current) {
        return ofMap(current, HashMap::new);
    }

    public static MapProvider ofDefault(Map<String, Object> map) {
        Objects.requireNonNull(map);
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
        if (raw == null) {
            if (targetType.isPrimitive()) {
                throw new IllegalArgumentException("Cannot convert null to primitive type: " + targetType.getName());
            }
            return null;  // allowed for reference types
        }

        // Already correct type → no conversion needed
        if (targetType.isInstance(raw)) {
            return F.cast(targetType.cast(raw));
        }

        // ───────────────────────────────────────────────────────────────
        // Number → Number coercion (the most common Gson fix)
        // ───────────────────────────────────────────────────────────────
        if (raw instanceof Number) {
            Number number = F.cast(raw);
            if (targetType == Integer.class || targetType == int.class) {
                return F.cast(number.intValue());
            }
            if (targetType == Long.class || targetType == long.class) {
                return F.cast(number.longValue());
            }
            if (targetType == Float.class || targetType == float.class) {
                return F.cast(number.floatValue());
            }
            if (targetType == Double.class || targetType == double.class) {
                return F.cast(number.doubleValue());
            }
            if (targetType == Short.class || targetType == short.class) {
                return F.cast(number.shortValue());
            }
            if (targetType == Byte.class || targetType == byte.class) {
                return F.cast(number.byteValue());
            }
        }

        // ───────────────────────────────────────────────────────────────
        // String → number (common when JSON had quoted numbers)
        // ───────────────────────────────────────────────────────────────
        if (raw instanceof String) {
            String str = F.cast(raw);
            str = str.trim();
            if (str.isEmpty()) {
                throw new IllegalArgumentException("Empty string cannot be converted to " + targetType.getName());
            }

            try {
                if (targetType == Integer.class || targetType == int.class) {
                    return (T) Integer.valueOf(str);
                }
                if (targetType == Long.class || targetType == long.class) {
                    return (T) Long.valueOf(str);
                }
                if (targetType == Float.class || targetType == float.class) {
                    return (T) Float.valueOf(str);
                }
                if (targetType == Double.class || targetType == double.class) {
                    return (T) Double.valueOf(str);
                }
                if (targetType == Boolean.class || targetType == boolean.class) {
                    return (T) Boolean.valueOf(str);
                }
                // You can add more (BigDecimal, BigInteger, etc.)
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("String '" + str + "' cannot be parsed as " + targetType.getName(), e);
            }
        }

        // ───────────────────────────────────────────────────────────────
        // Boolean coercion
        // ───────────────────────────────────────────────────────────────
        if (targetType == Boolean.class || targetType == boolean.class) {
            if (raw instanceof Boolean) {
                return (T) raw;
            }
            if (raw instanceof String) {
                String s = F.cast(raw);
                String lower = s.trim().toLowerCase();
                if ("true".equals(lower) || "1".equals(lower) || "yes".equals(lower)) {
                    return (T) Boolean.TRUE;
                }
                if ("false".equals(lower) || "0".equals(lower) || "no".equals(lower)) {
                    return (T) Boolean.FALSE;
                }
            }
            if (raw instanceof Number) {
                Number n = F.cast(raw);
                return F.cast(n.intValue() != 0);
            }
        }

        // Fallback: try simple cast (last resort)
        try {
            return (T) targetType.cast(raw);
        } catch (ClassCastException e) {
            throw new IllegalArgumentException(
                    "Cannot coerce value of type " + raw.getClass().getName()
                    + " to expected type " + targetType.getName()
                    + " (value: " + raw + ")", e);
        }
    }
}
