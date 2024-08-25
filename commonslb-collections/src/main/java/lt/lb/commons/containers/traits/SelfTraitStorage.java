package lt.lb.commons.containers.traits;

import java.util.function.Supplier;
import lt.lb.commons.Nulls;

/**
 *
 * @author laim0nas100
 */
public interface SelfTraitStorage extends WithTrait, TraitStorage {

    @Override
    public default <T> Trait<T> produceTrait(Object caller, Object signature, Supplier<T> initial) {
        Nulls.requireNonNulls(caller, signature, initial);
        return new BaseTrait.SimpleTrait<>(this, caller, signature, initial.get());
    }
    
}
