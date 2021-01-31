package lt.lb.commons.datasync.extractors;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.Supplier;
import lt.lb.commons.F;
import lt.lb.commons.containers.values.ValueProxy;
import lt.lb.commons.datasync.base.SimpleDataSyncDisplay;
import lt.lb.commons.datasync.base.SimpleDataSyncPersist;
import lt.lb.commons.func.unchecked.UncheckedConsumer;
import lt.lb.commons.func.unchecked.UncheckedRunnable;
import lt.lb.commons.func.unchecked.UncheckedSupplier;
import lt.lb.commons.reflect.beans.BasicBeanPropertyAccess;
import lt.lb.commons.reflect.FieldChain;
import lt.lb.commons.reflect.Refl;

/**
 *
 * @author laim0nas100
 */
public abstract class Extractors {

    public static <T> ValueProxy<T> ofUnsave(UncheckedSupplier<? extends T> sup, UncheckedConsumer<? super T> con) {
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

    public static <T> ValueProxy ofConstant(T constant) {
        return ofWriteIgnore(() -> constant);
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
                return F.uncheckedCall(() -> (T) chain.doGet(ob));
            }

            @Override
            public void set(T v) {
                F.uncheckedRun(() -> chain.doSet(ob, v));
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

    public static <T> ValueProxy<T> ofReflected(Object ob, String fieldName) {
        Class<? extends Object> aClass = ob.getClass();
        return F.uncheckedCall(() -> {
            Field field = aClass.getField(fieldName);
            return ofUnsave(
                    () -> (T) Refl.fieldAccessableGet(field, ob),
                    v -> Refl.fieldAccessableSet(field, ob, v)
            );
        });
    }

    public static <T> ValueProxy<T> ofBeanAccess(Object ob, String property) {
        return new BasicBeanPropertyAccess<>(ob, property);
    }

    public static SimpleDataSyncPersist ofSyncPersist(UncheckedRunnable run) {
        return new SimpleDataSyncPersist(ofReadIgnore(c -> run.run()));
    }

    public static SimpleDataSyncDisplay ofSyncDisplay(UncheckedRunnable run) {
        return new SimpleDataSyncDisplay(ofReadIgnore(c -> run.run()));
    }

    public static <T> SimpleDataSyncPersist<T> ofSimplePersistSync(ValueProxy<T> proxy) {
        return new SimpleDataSyncPersist(proxy);
    }

    public static <T> SimpleDataSyncPersist<T> ofSimplePersistSync(UncheckedRunnable read, UncheckedRunnable write) {
        return new SimpleDataSyncPersist(new ValueProxy<T>() {
            @Override
            public T get() {
                read.run();
                return null;
            }

            @Override
            public void set(T v) {
                write.run();
            }
        });
    }

    public static <T> SimpleDataSyncDisplay<T> ofSimpleDisplaySync(ValueProxy<T> proxy) {
        return new SimpleDataSyncDisplay(proxy);
    }

    public static <T> SimpleDataSyncDisplay<T> ofSimpleDisplaySync(UncheckedRunnable read, UncheckedRunnable write) {
        return new SimpleDataSyncDisplay(new ValueProxy<T>() {
            @Override
            public T get() {
                read.run();
                return null;
            }

            @Override
            public void set(T v) {
                write.run();
            }
        });
    }
}
