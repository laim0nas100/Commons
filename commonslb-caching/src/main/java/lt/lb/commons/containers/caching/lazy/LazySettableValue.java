package lt.lb.commons.containers.caching.lazy;

import java.util.List;
import lt.lb.commons.F;
import lt.lb.commons.containers.caching.Condition;
import lt.lb.commons.containers.caching.Dependency;
import lt.lb.commons.containers.values.Value;

/**
 * {@link LazyDependantValue} without a loader, but user has to set it. Set
 * value can only be updated manually. Good for lazily updated hierarchies as a
 * root node.
 *
 * @author laim0nas100
 */
public class LazySettableValue<T> extends LazyValue<T> {

    protected final Value<T> settable;

    public LazySettableValue(T value) {
        this(value, false);
    }

    public LazySettableValue(T value, boolean sync) {
        super(Dependency.of(new Value<>(value)), sync);
        WrappedDep<T, Value<T>> wrapped = F.cast(supply);
        settable = wrapped.implementation;
    }

    public LazySettableValue() {
        this(null);
    }

    protected LazySettableValue(Value<? extends T> supply, List<Condition> conditions, List<Dependency> dependents, boolean sync) {
        super(Dependency.of(supply), conditions, dependents, sync);
        WrappedDep<T, Value<T>> wrapped = F.cast(supply);
        settable = wrapped.implementation;
    }

    @Override
    public void fastSet(T val) {
        super.fastSet(settable.setAndGet(val));
    }

}
