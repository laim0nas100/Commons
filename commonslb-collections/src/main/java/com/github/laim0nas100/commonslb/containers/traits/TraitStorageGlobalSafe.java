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
public class TraitStorageGlobalSafe implements TraitStorage {

    public static final TraitStorageGlobalSafe INSTANCE = new TraitStorageGlobalSafe();

    private final Fetcher<Object, Fetcher> globalTraits = new SimpleMapFetcher<>(new WeakConcurrentHashMap(true));

    private final WeakConcurrentHashMap lockingMap = new WeakConcurrentHashMap<>();

    @Override
    public Fetcher<Object, Fetcher> getStorage() {
        return globalTraits;
    }

    @Override
    public <A> Trait<A> resolveInitial(Object caller, Object signature, Supplier<A> initial) {
        Object computeIfAbsent = lockingMap.computeIfAbsent(caller, a -> {
            Fetcher traitsForObject = getStorage().getOrCreate(caller, k -> Fetcher.hashMap());
            Object result = traitsForObject.getOrCreate(signature, k -> produceTrait(caller, k, initial));
            Trait trait = F.cast(result);
            trait.accept(initial.get());
            return result;
        });

        return F.cast(computeIfAbsent);
    }

    @Override
    public <T> Trait<T> produceTrait(Object caller, Object signature, Supplier<T> initialValue) {
        Nulls.requireNonNulls(caller, signature, initialValue);
        return new BaseTrait.SimpleTrait<>(this, caller, signature, initialValue.get());
    }

}
