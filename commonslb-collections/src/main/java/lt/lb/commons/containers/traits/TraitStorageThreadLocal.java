package lt.lb.commons.containers.traits;

import java.util.Map;
import java.util.function.Supplier;
import lt.lb.commons.Nulls;
import lt.lb.commons.containers.traits.Fetcher.MapFetcher;
import lt.lb.commons.containers.collections.WeakConcurrentHashMap;

/**
 *
 * @author laim0nas100
 */
public class TraitStorageThreadLocal implements TraitStorage {

    public static final TraitStorageThreadLocal INSTANCE = new TraitStorageThreadLocal();

    private final ThreadLocal<Fetcher<Object, Fetcher>> storage = ThreadLocal.withInitial(() -> new MapFetcher<>(new WeakConcurrentHashMap(true)));

    @Override
    public Fetcher<Object, Fetcher> getStorage() {
        return storage.get();
    }

    @Override
    public <T> Trait<T> produceTrait(Object caller, Object signature, Supplier<T> initial) {
        Nulls.requireNonNulls(caller,signature,initial);
        return new ThreadLocalTrait<>(this, caller, signature,initial.get());
    }
    
    

    public static class ThreadLocalTrait<A> extends BaseTrait<A> {

        public ThreadLocalTrait(TraitStorage storage, Object caller, Object signature, A initialValue) {
            super(storage, caller, signature);
            this.value = initialValue;
            
        }

    }

}
