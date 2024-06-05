package lt.lb.commons.containers.traits;

import java.util.function.Supplier;
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

    public static <A> Trait<A> globalInitial(Object caller, Object signature, Supplier<A> initialValue) {
        return TraitStorageGlobal.INSTANCE.resolveInitial(caller, signature, initialValue);
    }

    public static <A> Trait<A> globalThreadSafe(Object caller, Object signature) {
        return TraitStorageGlobalSafe.INSTANCE.resolve(caller, signature);
    }

    public static <A> Trait<A> globalThreadSafeInitial(Object caller, Object signature, Supplier<A> initialValue) {
        return TraitStorageGlobalSafe.INSTANCE.resolveInitial(caller, signature, initialValue);
    }

    public static <A> Trait<A> local(Object caller, Object signature) {
        return TraitStorageThreadLocal.INSTANCE.resolve(caller, signature);
    }

    public static <A> Trait<A> localInitial(Object caller, Object signature, Supplier<A> initialValue) {
        return TraitStorageThreadLocal.INSTANCE.resolveInitial(caller, signature, initialValue);
    }

}
