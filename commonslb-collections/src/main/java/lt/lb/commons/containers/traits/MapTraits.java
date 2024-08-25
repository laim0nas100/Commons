package lt.lb.commons.containers.traits;

import lt.lb.commons.F;
import lt.lb.commons.Nulls;
import lt.lb.commons.containers.traits.Fetcher.MapFetcher;

/**
 *
 * @author laim0nas100
 */
public interface MapTraits<K> extends TraitStorage, WithTrait {

    @Override
    public Fetcher<Object, Fetcher> getStorage();

    public K getKeyForTraits();
    
    public static class MapTraitsBase<K> implements MapTraits<K>, SelfTraitStorage{
        
        protected final K key;
        protected final MapFetcher<K,? extends MapTraits<K>> fetcher;

        public MapTraitsBase(K key, MapFetcher<K, ? extends MapTraits<K>> fetcher) {
            Nulls.requireNonNulls(key,fetcher);
            this.key = key;
            this.fetcher = fetcher;
        }

        @Override
        public Fetcher<Object, Fetcher> getStorage() {
            return F.cast(fetcher);
        }

        @Override
        public K getKeyForTraits() {
            return key;
        }
        
    }

}
