package com.github.laim0nas100.commonslb.containers.traits;

import java.util.Map;
import java.util.function.Supplier;
import com.github.laim0nas100.commonslb.Nulls;
import com.github.laim0nas100.commonslb.containers.traits.Fetcher.SimpleMapFetcher;
import com.github.laim0nas100.commonslb.containers.collections.WeakConcurrentHashMap;

/**
 *
 * @author laim0nas100
 */
public class TraitStorageThreadLocal implements TraitStorage {

    public static final TraitStorageThreadLocal INSTANCE = new TraitStorageThreadLocal();

    private final ThreadLocal<Fetcher<Object, Fetcher>> storage = ThreadLocal.withInitial(() -> new SimpleMapFetcher<>(new WeakConcurrentHashMap(true)));

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
