package lt.lb.commons.datasync.extractors;

import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.Supplier;
import lt.lb.commons.F;
import lt.lb.commons.containers.values.ValueProxy;
import lt.lb.commons.reflect.FieldChain;

/**
 *
 * @author laim0nas100
 */
public abstract class Extractors {
    
    public static <T> ValueProxy<Collection<T>> mutableCollection(Supplier<? extends Collection<T>> supl){
        return new ValueProxy<Collection<T>>() {
            @Override
            public Collection<T> get() {
                return supl.get();
            }

            @Override
            public void set(Collection<T> v) {
                Collection<T> get = supl.get();
                if(v == get){
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
}
