package lt.lb.commons.containers.caching;

import lt.lb.commons.F;
import lt.lb.commons.containers.values.Value;

/**
 * {@link LazyDependantValue} without a loader, but user has to set it. Set
 * value can only be updated manually. Good for lazily updated hierarchies as a
 * root node.
 *
 * @author laim0nas100
 */
public class LazySettableValue<T> extends LazyDependantValue<T> {

    protected final Value<T> settable;

    public LazySettableValue(T value) {
        super(new Value<>(value));
        settable = F.cast(supply);
    }

    public LazySettableValue() {
        super(new Value<>());
        settable = F.cast(supply);
    }

    @Override
    public void set(T val) {
        super.set(val);
        settable.set(val);
    }

}
