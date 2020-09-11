package lt.lb.commons.datasync.extractors;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;
import lt.lb.commons.F;
import lt.lb.commons.containers.values.ValueProxy;
import lt.lb.commons.func.unchecked.UnsafeConsumer;
import lt.lb.commons.func.unchecked.UnsafeSupplier;
import lt.lb.commons.reflect.FieldChain;

/**
 *
 * @author laim0nas100
 */
public abstract class Extractors {

    public static <T> ValueProxy<T> ofUnsave(UnsafeSupplier<? extends T> sup, UnsafeConsumer<? super T> con) {
        return quickProxy(sup, con);
    }

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

    public static <T> ValueProxy<T> ofReadConstant(T constant, Consumer<? super T> cons) {
        return new ValueProxy<T>() {
            @Override
            public T get() {
                return constant;
            }

            @Override
            public void set(T v) {
                cons.accept(v);
            }
        };
    }

    public static <T> ValueProxy<T> ofReadIgnore(Consumer<? super T> cons) {
        return ofReadConstant(null, cons);
    }

    public static <T> ValueProxy<T> ofWriteIgnore(Supplier<? extends T> supl) {
        return new ValueProxy<T>() {
            @Override
            public T get() {
                return supl.get();
            }

            @Override
            public void set(T v) {
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

    public static class BasicReadPropertyAccess<V, T> implements Supplier<T> {

        protected V object;
        protected Method read;
        protected String readMethodName;

        @Override
        public T get() {
            return F.unsafeCall(() -> (T) read.invoke(object));
        }

        public BasicReadPropertyAccess(V object, String property) {
            this.object = object;
            Class clazz = object.getClass();
            String cap = BasicBeanPropertyAccess.capitalize(property);
            String simpleReadName = "get" + cap;

            Optional<Method> firstTry = Stream.of(clazz.getMethods())
                    .filter(p -> p.getName().equals(simpleReadName))
                    .filter(p -> p.getParameterCount() == 0)
                    .findFirst();

            if (!firstTry.isPresent()) {
                readMethodName = "is" + BasicBeanPropertyAccess.capitalize(property);
                read = Stream.of(clazz.getMethods())
                        .filter(p -> p.getName().equals(readMethodName))
                        .filter(p -> p.getParameterCount() == 0)
                        .findFirst().orElseThrow(() -> new IllegalArgumentException("Failed to find read method of name:" + simpleReadName + " or " + readMethodName));
            } else {
                read = firstTry.get();
                readMethodName = simpleReadName;
            }

        }
    }

    public static class BasicWritePropertyAccess<V, T> implements Consumer<T> {

        protected V object;
        protected Method write;
        protected String writeMethodName;

        @Override
        public void accept(T v) {
            F.unsafeRun(() -> {
                write.invoke(object, v);
            });
        }

        public BasicWritePropertyAccess(V object, String property) {
            this.object = object;
            Class clazz = object.getClass();
            //try simple then boolean
            writeMethodName = "set" + BasicBeanPropertyAccess.capitalize(property);

            write = Stream.of(clazz.getMethods())
                    .filter(p -> p.getName().equals(writeMethodName))
                    .filter(p -> p.getParameterCount() == 1)
                    .findFirst().orElseThrow(() -> new IllegalArgumentException("Failed to find write method of name:" + writeMethodName));
        }
    }

    public static class BasicBeanPropertyAccess<V, T> implements ValueProxy<T> {

        protected BasicWritePropertyAccess<V, T> write;
        protected BasicReadPropertyAccess<V, T> read;

        @Override
        public T get() {
            return read.get();
        }

        @Override
        public void set(T v) {
            write.accept(v);
        }

        public BasicBeanPropertyAccess(V object, String property) {
            write = new BasicWritePropertyAccess<>(object, property);
            read = new BasicReadPropertyAccess<>(object, property);
        }
        
        /**
         * Returns a String which capitalizes the first letter of the string.
         */
        public static String capitalize(String name) {
            if (name == null || name.length() == 0) {
                return name;
            }
            return name.substring(0, 1).toUpperCase(Locale.ENGLISH) + name.substring(1);
        }


    }
}
