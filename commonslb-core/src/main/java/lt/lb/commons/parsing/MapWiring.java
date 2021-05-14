package lt.lb.commons.parsing;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Collection;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import lt.lb.commons.ArrayOp;
import lt.lb.commons.Ins;
import lt.lb.uncheckedutils.Checked;
import lt.lb.uncheckedutils.SafeOpt;

/**
 *
 * @author laim0nas100
 */
public interface MapWiring {

    public default <T> T populateObject(Supplier<T> supl, Map map) {
        T obj = supl.get();
        return populateObject(obj, map);
    }

    public <T> T populateObject(T val, Map map);

    public <T> Map populateMap(T val, Map map);

    public default <T> Map populateMap(T val) {
        return populateMap(val, new HashMap<>());
    }

    public static class SimpleStringMapWiring implements MapWiring {

        public Function<Integer, List> listConstructor = ArrayList::new;
        public Function<Integer, Set> setConstructor = HashSet::new;
        public Function<Integer, Queue> queueConstructor = size -> new LinkedList();
        public Function<Integer, Deque> dequeConstructor = size -> new LinkedList();
        public boolean noSkip = true;

        public boolean skipNullFields = false;
        public boolean wireToString = false;
        public boolean allowDublicateKeyOverride = false;

        public <T> Map populateMap(T object, Map map) {
            Objects.requireNonNull(object);
            Objects.requireNonNull(map);
            Class clazz = object.getClass();
            Field[] fields = clazz.getFields();
            for (Field f : fields) {
                serializeField(object, f, map);
            }
            return map;
        }

        @Override
        public <T> T populateObject(T object, Map map) {
            Objects.requireNonNull(object);
            Objects.requireNonNull(map);
            Class clazz = object.getClass();
            Field[] fields = clazz.getFields();
            for (Field f : fields) {
                parseField(object, f, map);
            }
            return object;
        }

        public <T> void serializeField(T obj, Field field, Map map) {
            Param[] param = field.getAnnotationsByType(Param.class);

            if (param.length != 1) {
                return;
            }

            Param name = param[0];
            String key = name.value();
            if (!allowDublicateKeyOverride && map.containsKey(key)) {
                throw new IllegalStateException("Dublicate key:" + key);
            }
            Checked.uncheckedRun(() -> {
                Object value = field.get(obj);
                putValue(key, value, map);
            });
        }

        public void putValue(String key, Object value, Map map) {
            if (skipNullFields && value == null) {
                return;
            }
            if (wireToString) {
                map.put(key, String.valueOf(value));
                return;
            }
            map.put(key, value);
        }

        public <T> void parseField(T obj, Field field, Map map) {
            Param[] param = field.getAnnotationsByType(Param.class);

            if (param.length != 1) {
                return;
            }

            Param name = param[0];
            String key = name.value();
            boolean contains = map.containsKey(key);

            if (noSkip && !contains) {
                throw new IllegalArgumentException("Failed to find a parameter:" + name.value());
            }
            Object value = map.get(key);
            if (skipNullFields && value == null) {
                return;
            }
            final String finalStr = String.valueOf(value);
            Checked.uncheckedRun(() -> {
                SafeOpt parsed = parse(finalStr, field.getType(), () -> getGenericParamType(field));
                parsed.throwIfErrorAsNested();
                Object parsedValue = parsed.orNull();
                field.set(obj, parsedValue);
            });

        }

        public SafeOpt<Class[]> getGenericParamType(Field field) {
            return SafeOpt.of(field).map(m -> m.getGenericType())
                    .select(ParameterizedType.class).map(m -> m.getActualTypeArguments())
                    .filter(m -> m.length > 0)
                    .map(types -> ArrayOp.castArray(types, Class.class));
        }

        public SafeOpt parseField(String str, Class type) {
            StringParser<String> parser = getStringParser(type);
            Ins.InsCl<?> ins = Ins.ofPrimitivePromotion(type);
            if (ins.instanceOf(String.class)) {
                return parser.parseOptString(str);
            } else if (ins.instanceOf(Boolean.class)) {
                return parser.parseOptBool(str);

            } else if (ins.instanceOf(Character.class)) {
                return parser.parseOptString(str).map(m -> m.charAt(0));
            } else if (ins.instanceOfAny(Byte.class, Short.class, Integer.class, Long.class)) {
                return parser.parseOptLong(str).map(val -> {
                    if (ins.instanceOf(Byte.class)) {
                        return val.byteValue();
                    } else if (ins.instanceOf(Short.class)) {
                        return val.shortValue();
                    } else if (ins.instanceOf(Integer.class)) {
                        return val.intValue();
                    } else {
                        return val;
                    }
                });
            } else if (ins.instanceOfAny(Float.class, Double.class)) {
                return parser.parseOptDouble(str).map(val -> {
                    if (ins.instanceOf(Float.class)) {
                        return val.floatValue();
                    } else {
                        return val;
                    }
                });
            } else {
                return SafeOpt.error(new IllegalArgumentException("Unsupported type :" + type));
            }
        }

        public SafeOpt<Collection> parseCollection(String str, Class genType, Function<Integer, ? extends Collection> func) {
            StringParser<String> parser = getStringParser(genType);
            return parser.parseOptStringList(str).map(list -> {
                Collection newCollection = func.apply(list.size());
                list.stream()
                        .map(strItem -> {
                            return parseField(strItem, genType).throwIfErrorAsNested();
                        })
                        .filter(s -> s.isPresent())
                        .map(s -> s.get())
                        .forEachOrdered(newCollection::add);

                return newCollection;
            });
        }

        public SafeOpt parse(String str, Class type, Supplier<SafeOpt<Class[]>> genericType) {
            Ins.InsCl<?> ins = Ins.ofPrimitivePromotion(type);

            if (ins.instanceOf(Collection.class)) {
                Class genType = genericType.get().map(types -> types[0]).orElse(String.class);
                if (ins.instanceOf(List.class)) {
                    return parseCollection(str, genType, listConstructor);
                } else if (ins.instanceOf(Set.class)) {
                    return parseCollection(str, genType, setConstructor);
                } else if (ins.instanceOf(Queue.class)) {
                    return parseCollection(str, genType, queueConstructor);
                } else if (ins.instanceOf(Deque.class)) {
                    return parseCollection(str, genType, dequeConstructor);
                } else {
                    return SafeOpt.error(new IllegalArgumentException("Unsupported collection type " + type));
                }
            } else {
                return parseField(str, type);
            }
        }

        public static class NullCheckStringParser implements StringParser<String> {

            public final String nullValue = "null";
            
            @Override
            public SafeOpt<String> parseOptString(String p) {
                if (p == null) {
                    return SafeOpt.empty();
                }
                if (nullValue.equals(p)) {
                    return SafeOpt.empty();
                }
                return SafeOpt.of(p);
            }


        }

        public NullCheckStringParser parser = new NullCheckStringParser();

        public StringParser<String> getStringParser(Class type) {
            return parser;
        }

    }
}
