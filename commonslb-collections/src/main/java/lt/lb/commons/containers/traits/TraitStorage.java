package lt.lb.commons.containers.traits;

import lt.lb.commons.F;

/**
 *
 * @author laim0nas100
 */
public interface TraitStorage {

    Fetcher<Object, Fetcher> getStorage();

    default <A> Trait<A> resolve(Object caller, Object signature) {
        Fetcher traitsForObject = getStorage().getOrCreate(caller, k -> Fetcher.hashMap());
        Object result = traitsForObject.getOrCreate(signature, k -> produceTrait(caller, k));
        return F.cast(result);
    }

    <T> Trait<T> produceTrait(Object caller, Object signature);

}
