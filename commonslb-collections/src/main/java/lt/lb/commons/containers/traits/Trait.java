package lt.lb.commons.containers.traits;

import lt.lb.commons.containers.values.ValueProxy;

/**
 *
 * Add properties to your interfaces. By default using weak object references.
 * cleanup.
 *
 * @author laim0nas100
 */
public interface Trait<T> extends ValueProxy<T> {

    public Fetcher resolveTraits();

    public Object resolveSignature();

    public static <A> Trait<A> custom(TraitStorage storage, Object caller, Object signature) {
        return storage.resolve(caller, signature);
    }

    public static <A> Trait<A> global(Object caller, Object signature) {
        return TraitStorageGlobal.INSTANCE.resolve(caller, signature);
    }

    public static <A> Trait<A> local(Object caller, Object signature) {
        return TraitStorageThreadLocal.INSTANCE.resolve(caller, signature);
    }

}
