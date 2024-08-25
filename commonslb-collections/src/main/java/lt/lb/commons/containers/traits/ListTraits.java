package lt.lb.commons.containers.traits;

import lt.lb.commons.F;
import lt.lb.commons.Nulls;
import lt.lb.commons.containers.traits.Fetcher.ListFetcher;

/**
 *
 * @author laim0nas100
 */
public interface ListTraits extends TraitStorage, WithTrait {

    @Override
    public default Fetcher<Object, Fetcher> getStorage() {
        return F.cast(getLocalStorage());
    }

    public ListFetcher< ? extends ListTraits> getLocalStorage();

    public int getIndexForTraits();

    public static class ListTraitsBase implements ListTraits, SelfTraitStorage {

        protected final int index;
        protected final ListFetcher fetcher;

        public ListTraitsBase(int index, ListFetcher fetcher) {
            this.index = index;
            this.fetcher = Nulls.requireNonNull(fetcher);
        }
        
        
        @Override
        public ListFetcher<? extends ListTraits> getLocalStorage() {
            return fetcher;
        }

        @Override
        public int getIndexForTraits() {
            return index;
        }

    }

}
