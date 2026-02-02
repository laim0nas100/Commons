package lt.lb.commons.refmodel.maps;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import lt.lb.commons.F;
import lt.lb.commons.Ins;
import lt.lb.commons.parsing.numbers.FastParse;
import lt.lb.commons.refmodel.RefNotation;
import lt.lb.prebuiltcollections.DelegatingMap;
import lt.lb.uncheckedutils.PassableException;
import lt.lb.uncheckedutils.SafeOpt;

/**
 *
 * @author laim0nas100
 */
public class MapProvider implements DelegatingMap<String, Object> {

    protected Map<String, Object> mainMap;

    public MapProvider() {
        this(new LinkedHashMap<>());
    }

    public MapProvider(Map<String, Object> mainMap) {
        this.mainMap = Objects.requireNonNull(mainMap);
    }

    @Override
    public Map<String, Object> delegate() {
        return mainMap;
    }

    public Map<String, Object> createMap() {
        return new LinkedHashMap<>();
    }

    public List createList() {
        return new ArrayList<>();
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

    public void mergeWith(MapProvider other) {
        MapProvider.deepMergeMaps(delegate(), other.delegate());
    }

    public <T> SafeOpt<T> read(ObjectRef<T> ref) {
        return readRemoveConvert(ref, false);
    }

    public SafeOpt read(RefNotation notation, String fullPath) {
        return readRemove(notation, fullPath, false);
    }

    public <T> SafeOpt<T> remove(ObjectRef<T> ref) {
        return readRemove(ref.getNotation(), ref.getRelative(), true);
    }

    public SafeOpt remove(RefNotation notation, String fullPath) {
        return readRemove(notation, fullPath, true);
    }

    /**
     * Read or remove member then convert
     *
     * @param <T>
     * @param provider
     * @param remove
     * @return
     */
    protected <T> SafeOpt readRemoveConvert(ObjectRef<T> ref, boolean remove) {
        SafeOpt<Object> readRemove = readRemove(ref.getNotation(), ref.getRelative(), remove);
        Class[] param = ref.getParameterTypes();
        if (param.length == 1) {
            return readRemove.map(m -> coerceFromRaw(m, param[0]));
        } else {
            return readRemove;
        }
    }

    protected <T> SafeOpt<T> readRemove(RefNotation notation, String fullPath, boolean remove) {
        Map<String, Object> map = delegate();

        List<String> steps = notation.steps(fullPath);
        StringBuilder pathBuilder = new StringBuilder();
        Map parent = map;
        for (int i = 0; i < steps.size(); i++) {
            String step = steps.get(i);
            pathBuilder.append(step);
            Optional<Integer> indexFromStep = notation.getIndexFromStep(step);
            boolean indexed = indexFromStep.isPresent();
            if (indexed) {
                step = notation.getMemberWithoutIndex(step);
            }
            boolean last = i == steps.size() - 1;
            if (last) {
                if (!parent.containsKey(step)) {
                    if (indexed) {
                        return err("Failed to read:" + fullPath + "no list, failed at:" + pathBuilder.toString());
                    } else {
                        return SafeOpt.empty();
                    }
                } else { //contains key
                    if (indexed) {
                        int index = indexFromStep.get();
                        Object get = parent.get(step);
                        if (get instanceof List) {
                            List list = F.cast(get);
                            index = index < 0 ? index + list.size() + 1 : index;
                            if (index >= 0 && index < list.size()) {
                                if (remove) {
                                    return (SafeOpt) SafeOpt.ofNullable(list.remove(index));
                                } else {
                                    return (SafeOpt) SafeOpt.ofNullable(list.get(index));
                                }
                            }
                        } else {
                            return err("Failed to read:" + fullPath + " index out of bounds, failed at:" + pathBuilder.toString());
                        }
                    } else {
                        if (remove) {
                            return (SafeOpt) SafeOpt.ofNullable(parent.remove(step));
                        } else {
                            return (SafeOpt) SafeOpt.ofNullable(parent.get(step));
                        }

                    }
                }
            }// not last

            if (!parent.containsKey(step)) {
                if (indexed) {
                    return err("Failed to read:" + fullPath + " expected a list, failed at:" + pathBuilder.toString());
                } else {
                    return err("Failed to read:" + fullPath + " expected a map, failed at:" + pathBuilder.toString());
                }

            }
            if (indexed) {

                Object listRead = parent.get(step);
                if (listRead instanceof List) {
                    List list = F.cast(listRead);
                    int index = indexFromStep.get();
                    if (index <= list.size() && !list.isEmpty()) {
                        Object get;
                        index = index < 0 ? index + list.size() + 1 : index;
                        if (index >= 0 && index < list.size()) {
                            get = list.get(index);
                            if (get instanceof Map) {
                                parent = F.cast(get);
                            } else {
                                return err("Failed to read:" + fullPath + " expected a map, failed at:" + pathBuilder.toString());
                            }
                        } else {
                            return err("Failed to read:" + fullPath + " index out of bounds failed at:" + pathBuilder.toString());
                        }

                    } else {
                        return err("Failed to read:" + fullPath + " index out of bounds failed at:" + pathBuilder.toString());
                    }
                } else {
                    return err("Failed to read:" + fullPath + " expected a list, failed at:" + pathBuilder.toString());
                }
            } else {
                Object get = parent.getOrDefault(step, null);
                if (get instanceof Map) {
                    parent = F.cast(get);
                } else {
                    return err("Failed to read:" + fullPath + " expected a map, failed at:" + pathBuilder.toString());
                }

            }

        }
        return err("Failed to read:" + fullPath);
    }

    public <T> SafeOpt<T> write(ObjectRef<T> ref, T value) {
        return write(ref.getNotation(), ref.getRelative(), value);
    }

    /**
     * Writes a value at resolved path, creating a map object if the entry is
     * nested
     *
     * @return SafeOpt of previous value or map traversal error
     */
    public <T> SafeOpt<T> write(RefNotation notation, String fullPath, T value) {
        Map<String, Object> map = delegate();

        List<String> steps = notation.steps(fullPath);
        StringBuilder pathBuilder = new StringBuilder();
        Map parent = map;
        for (int i = 0; i < steps.size(); i++) {
            String step = steps.get(i);
            pathBuilder.append(step);

            Optional<Integer> indexFromStep = notation.getIndexFromStep(step);
            boolean indexed = indexFromStep.isPresent();
            if (indexed) {
                step = notation.getMemberWithoutIndex(step);
            }
            boolean last = i == steps.size() - 1;

            //last node can be null or another map or a list
            if (last) {
                Object replaced = null;
                if (!parent.containsKey(step)) {
                    if (indexed) {
                        List createList = createList();
                        createList.add(value);
                        parent.put(step, createList);
                    } else {
                        parent.put(step, value);
                    }
                } else { //contains key, rewrite
                    if (indexed) {
                        int index = indexFromStep.get();
                        Object get = parent.get(step);
                        if (get instanceof List) {
                            List list = F.cast(get);
                            index = index < 0 ? index + list.size() + 1 : index;
                            if (index >= 0 && index < list.size()) {
                                replaced = list.set(index, value);
                            } else if (index == -1 || index == list.size()) {
                                list.add(value);
                            }
                        } else {
                            return err("Failed to write:" + fullPath + " not at list, failed at:" + pathBuilder.toString());
                        }
                    } else {
                        replaced = parent.put(step, value);
                    }

                }
                return SafeOpt.ofNullable(F.cast(replaced));
                // not last
            } else {
                if (parent.containsKey(step)) { //has structure
                    Object entry = parent.get(step);
                    if (indexed) {
                        int index = indexFromStep.get();
                        if (entry instanceof List) {
                            List list = F.cast(entry);
                            index = index < 0 ? index + list.size() + 1 : index;

                            if (index >= 0 && index < list.size()) {
                                Object get = list.get(index);
                                if (get instanceof Map) {
                                    parent = F.cast(get);
                                } else {
                                    return err("Failed to write:" + fullPath + " not a map, failed at:" + pathBuilder.toString());
                                }
                            } else if (index == -1 || index == list.size()) { //append new
                                parent = createMap();
                                list.add(parent);

                            } else {
                                return err("Failed to write:" + fullPath + " index out of bound failed at:" + pathBuilder.toString());

                            }
                        } else {
                            return err("Failed to write:" + fullPath + " expected list failed at:" + pathBuilder.toString());

                        }
                    } else {//not indexed
                        if (entry instanceof Map) {
                            parent = F.cast(entry);
                        } else {
                            return err("Failed to write:" + fullPath + " expected map failed at:" + pathBuilder.toString());

                        }
                    }
                } else { //no structure and not last
                    Map<String, Object> newParent = createMap();
                    if (indexed) {
                        List list = createList();
                        int index = indexFromStep.get();
                        if (index == 0 || index == -1) {
                            list.add(newParent);
                            parent.put(step, list);
                        } else {
                            return err("Failed to write:" + fullPath + " expected new list indexed at the end failed at:" + pathBuilder.toString());
                        }
                    } else {
                        parent.put(step, newParent);
                    }
                    parent = newParent;
                }
            }
        }

        return err("Failed to write:" + fullPath);
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
    protected <T> T coerceFromRaw(Object raw, Class<T> targetType) {
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

    public LinkedHashMap<String, Object> flatten(RefNotation notation) {
        return flatten("", notation);
    }

    public LinkedHashMap<String, Object> flatten(String parentPath, RefNotation notation) {
        Map<String, Object> delegate = delegate();
        LinkedHashMap<String, Object> flat = new LinkedHashMap<>();
        recursiveFlatten(flat, delegate, parentPath, notation);
        return flat;
    }

    public static void recursiveFlatten(Map<String, Object> flat, Map<String, Object> map, String parentPath, RefNotation notation) {
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String key = entry.getKey();
            Object val = entry.getValue();
            if (val == null) {
                continue;
            }
            String relation = notation.produceRelation(parentPath, key);
            if (val instanceof List) {
                List list = F.cast(val);
                for (int i = 0; i < list.size(); i++) {
                    Object item = list.get(i);
                    String arrayAccess = notation.produceArrayAccess(relation, i);
                    if (item instanceof Map) {
                        recursiveFlatten(flat, F.cast(item), arrayAccess, notation);
                    } else if (item instanceof List) {
                        throw new IllegalStateException("List inside a list is not supported, at:" + arrayAccess);
                    } else {
                        flat.put(arrayAccess, val);
                    }
                }

            } else if (val instanceof Map) {
                recursiveFlatten(flat, F.cast(val), relation, notation);

            } else {//just a simple member
                flat.put(relation, val);
            }
        }
    }

    private static <T> SafeOpt<T> err(String str) {
        return SafeOpt.error(new PassableException(str));
    }
}
