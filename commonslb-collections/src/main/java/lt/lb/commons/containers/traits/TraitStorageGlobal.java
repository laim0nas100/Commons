package lt.lb.commons.containers.traits;

import lt.lb.commons.containers.traits.Fetcher.MapFetcher;
import lt.lb.commons.containers.collections.WeakConcurrentHashMap;

/**
 *
 * @author laim0nas100
 * By default stores into one map (static instance), but can be used individually as need be.
 * 
 */
public class TraitStorageGlobal implements TraitStorage {

    public static final TraitStorageGlobal INSTANCE = new TraitStorageGlobal();

    private final Fetcher<Object, Fetcher> globalTraits = new MapFetcher<>(new WeakConcurrentHashMap(true));

    @Override
    public Fetcher<Object, Fetcher> getStorage() {
        return globalTraits;
    }

    @Override
    public <T> Trait<T> produceTrait(Object caller, Object signature) {
        return new GloballySavedTrait<>(this, caller, signature);
    }

    public static class GloballySavedTrait<A> extends BaseTrait<A> {

        public GloballySavedTrait(TraitStorage storage, Object caller, Object signature) {
            super(storage, caller, signature);
        }
    }

}
