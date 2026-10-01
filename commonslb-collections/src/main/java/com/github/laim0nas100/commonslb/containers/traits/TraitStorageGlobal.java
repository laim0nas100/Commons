package com.github.laim0nas100.commonslb.containers.traits;

import java.util.function.Supplier;
import com.github.laim0nas100.commonslb.F;
import com.github.laim0nas100.commonslb.Nulls;
import com.github.laim0nas100.commonslb.containers.traits.Fetcher.SimpleMapFetcher;
import com.github.laim0nas100.commonslb.containers.collections.WeakConcurrentHashMap;

/**
 *
 * @author laim0nas100 By default stores into one map (static instance), but can
 * be used individually as need be.
 *
 */
public class TraitStorageGlobal implements TraitStorage {

    public static final TraitStorageGlobal INSTANCE = new TraitStorageGlobal();

    private final Fetcher<Object, Fetcher> globalTraits = new SimpleMapFetcher<>(new WeakConcurrentHashMap(true));

    @Override
    public Fetcher<Object, Fetcher> getStorage() {
        return globalTraits;
    }

    @Override
    public <A> Trait<A> resolveInitial(Object caller, Object signature, Supplier<A> initialValue) {
        Nulls.requireNonNull(initialValue);
        Fetcher traitsForObject = getStorage().getOrCreate(caller, k -> Fetcher.hashMap());
        Object result = traitsForObject.getOrCreate(signature, k -> produceTrait(caller, k, initialValue));
        return F.cast(result);
    }

    @Override
    public <T> Trait<T> produceTrait(Object caller, Object signature, Supplier<T> initialValue) {
        Nulls.requireNonNulls(caller,signature,initialValue);
        return new BaseTrait.SimpleTrait<>(this, caller, signature, initialValue.get());
    }


}
