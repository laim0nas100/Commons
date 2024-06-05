package lt.lb.commons.containers.traits;

import java.util.function.Supplier;
import lt.lb.commons.F;

/**
 *
 * @author laim0nas100
 */
public interface TraitStorage {

    Fetcher<Object, Fetcher> getStorage();

    default <A> Trait<A> resolve(Object caller, Object signature) {
        return resolveInitial(caller, signature, () -> null);
    }

    default <A> Trait<A> resolveInitial(Object caller, Object signature, Supplier<A> initial) {
        Fetcher traitsForObject = getStorage().getOrCreate(caller, k -> Fetcher.hashMap());
        Object result = traitsForObject.getOrCreate(signature, k -> produceTrait(caller, k, initial));
        return F.cast(result);
    }

    <T> Trait<T> produceTrait(Object caller, Object signature, Supplier<T> initial);

    default <T> Trait<T> produceTrait(Object caller, Object signature) {
        return produceTrait(caller, signature, () -> null);
    }

}
