package lt.lb.commons.datasync.extractors;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.IdentityHashMap;
import static java.util.Locale.ENGLISH;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;
import lt.lb.commons.F;
import lt.lb.commons.containers.caching.LazyValue;
import lt.lb.commons.containers.values.ValueProxy;
import lt.lb.commons.reflect.FieldChain;

/**
 *
 * @author laim0nas100
 */
public abstract class Extractors {

    public static <T> ValueProxy<Collection<T>> mutableCollection(Supplier<? extends Collection<T>> supl) {
        return new ValueProxy<Collection<T>>() {
            @Override
            public Collection<T> get() {
                return supl.get();
            }

            @Override
            public void set(Collection<T> v) {
                Collection<T> get = supl.get();
                if (v == get) {
                    //same collection, just return
                    return;
                }
                get.clear();
                get.addAll(v);

            }
        };
    }

    public static <T> ValueProxy<T> castProxy(Supplier supl, Consumer cons) {
        return new ValueProxy<T>() {
            @Override
            public T get() {
                return F.cast(supl.get());
            }

            @Override
            public void set(T v) {
                cons.accept(v);
            }
        };
    }

    public static <T> ValueProxy<T> quickProxy(Supplier<? extends T> supl, Consumer<? super T> cons) {
        return new ValueProxy<T>() {
            @Override
            public T get() {
                return supl.get();
            }

            @Override
            public void set(T v) {
                cons.accept(v);
            }
        };
    }

    public static <T> ValueProxy<T> chainToProxy(Object ob, String chain) {
        return chainToProxy(ob, FieldChain.ObjectFieldChain.ofChainParse(chain));
    }

    public static <T> ValueProxy<T> chainToProxy(Object ob, FieldChain.ObjectFieldChain chain) {
        return new ValueProxy<T>() {
            @Override
            public T get() {
                return F.unsafeCall(() -> (T) chain.doGet(ob));
            }

            @Override
            public void set(T v) {
                F.unsafeRun(() -> chain.doSet(ob, v));
            }
        };
    }

    public static class ChainProxy<V, T> implements ValueProxy<T> {

        private final ValueProxy<T> proxy;
        private final V object;

        public ChainProxy(V ob, String path) {
            object = ob;
            proxy = chainToProxy(ob, FieldChain.ObjectFieldChain.ofChainParse(path));
        }

        public V getObject() {
            return object;
        }

        @Override
        public T get() {
            return proxy.get();
        }

        @Override
        public void set(T v) {
            proxy.set(v);
        }
    }

    public static class BasicBeanPropertyAccess<V, T> implements ValueProxy<T> {

        protected final V object;
        protected final Method read;
        protected final Method write;
        protected final String writeMethodName;
        protected final String readMethodName;

        @Override
        public T get() {
            return F.unsafeCall(() -> (T) read.invoke(object));
        }

        @Override
        public void set(T v) {
            F.unsafeRun(() -> {
                write.invoke(object, v);
            });
        }

        public BasicBeanPropertyAccess(V object, String property) {
            this.object = object;
            Class clazz = object.getClass();
            //try simple then boolean
            writeMethodName = "set" + NameGenerator.capitalize(property);

            write = Stream.of(clazz.getMethods())
                    .filter(p -> p.getName().equals(writeMethodName))
                    .filter(p -> p.getParameterCount() == 1)
                    .findFirst().orElseThrow(() -> new IllegalArgumentException("Failed to find write method of name:" + writeMethodName));

            String simpleReadName = "get" + NameGenerator.capitalize(property);

            Optional<Method> firstTry = Stream.of(clazz.getMethods())
                    .filter(p -> p.getName().equals(simpleReadName))
                    .filter(p -> p.getParameterCount() == 0)
                    .findFirst();

            if (!firstTry.isPresent()) {
                readMethodName = "is" + NameGenerator.capitalize(property);
                read = Stream.of(clazz.getMethods())
                        .filter(p -> p.getName().equals(readMethodName))
                        .filter(p -> p.getParameterCount() == 0)
                        .findFirst().orElseThrow(() -> new IllegalArgumentException("Failed to find write method of name:" + simpleReadName + " or " + readMethodName));
            } else {
                read = firstTry.get();
                readMethodName = simpleReadName;
            }

        }

    }

    private static class NameGenerator {

        private Map<Object, String> valueToName;
        private Map<String, Integer> nameToCount;

        public NameGenerator() {
            valueToName = new IdentityHashMap<>();
            nameToCount = new HashMap<>();
        }

        /**
         * Clears the name cache. Should be called to near the end of the
         * encoding cycle.
         */
        public void clear() {
            valueToName.clear();
            nameToCount.clear();
        }

        /**
         * Returns the root name of the class.
         */
        @SuppressWarnings("rawtypes")
        public static String unqualifiedClassName(Class type) {
            if (type.isArray()) {
                return unqualifiedClassName(type.getComponentType()) + "Array";
            }
            String name = type.getName();
            return name.substring(name.lastIndexOf('.') + 1);
        }

        /**
         * Returns a String which capitalizes the first letter of the string.
         */
        public static String capitalize(String name) {
            if (name == null || name.length() == 0) {
                return name;
            }
            return name.substring(0, 1).toUpperCase(ENGLISH) + name.substring(1);
        }

        /**
         * Returns a unique string which identifies the object instance.
         * Invocations are cached so that if an object has been previously
         * passed into this method then the same identifier is returned.
         *
         * @param instance object used to generate string
         * @return a unique string representing the object
         */
        public String instanceName(Object instance) {
            if (instance == null) {
                return "null";
            }
            if (instance instanceof Class) {
                return unqualifiedClassName((Class) instance);
            } else {
                String result = valueToName.get(instance);
                if (result != null) {
                    return result;
                }
                Class<?> type = instance.getClass();
                String className = unqualifiedClassName(type);

                Integer size = nameToCount.get(className);
                int instanceNumber = (size == null) ? 0 : size + 1;
                nameToCount.put(className, instanceNumber);

                result = className + instanceNumber;
                valueToName.put(instance, result);
                return result;
            }
        }
    }
}
