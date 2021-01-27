package lt.lb.commons.containers.caching;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import lt.lb.commons.containers.values.Value;

/**
 *
 * {@inheritDoc}
 * {@link LazyValue} with dependant connections.
 */
public class LazyDependantValue<T> extends LazyValue<T> {

    public LazyDependantValue(Supplier<? extends T> supply) {
        super(supply);
    }

    public LazyDependantValue(T value) {
        super(value);
    }

    /**
     * Creates a new dependant child that has to be always updated (or
     * reupdated) after this one
     *
     * @param <U>
     * @param <L>
     * @param child
     * @return
     */
    public <U, L extends LazyValue<U>> L createDependantChild(L child) {
        return createDependantChild(child, false);
    }

    /**
     * Creates a new dependant child that has to be always updated (or
     * reupdated) after this one with parameter which determines whether parent
     * is being called in in child's supplier, so that no repeated parent call
     * must be made
     *
     * @param <U>
     * @param <L>
     * @param child
     * @param addParentInSupply wether child gets this value as an explicit
     * dependency
     * @return
     */
    public <U, L extends LazyValue<U>> L createDependantChild(L child, boolean addParentInSupply) {
        child.addCondition(Condition.ensureLoadOrder(this, child));

        if (addParentInSupply) {
            child.addDependency(this);
        }
        return child;
    }

    /**
     * Create explicit mapping that updates child when parent is updated
     *
     * @param <U>
     * @param mapper
     * @return
     */
    public <U> LazyDependantValue<U> map(Function<? super T, ? extends U> mapper) {
        LazyDependantValue<T> me = this;
        LazyDependantValue<U> lazyDepVal = new LazyDependantValue<>(() -> mapper.apply(me.get()));
        return createDependantChild(lazyDepVal, false);
    }

    /**
     * Create explicit mapping that updates child when parent is updated. Also
     * can decide how to update value based on currently held one.
     *
     * @param <U>
     * @param startingValue
     * @param mapper
     * @return
     */
    public <U> LazyDependantValue<U> map(U startingValue, BiFunction<U, ? super T, ? extends U> mapper) {
        LazyDependantValue<T> me = this;
        Value<U> savedValue = new Value<>(startingValue);
        LazyDependantValue<U> child = new LazyDependantValue<>(savedValue);
        child.addCondition(Condition.ensureLoadOrder(me, child));
        child.addDependency(() -> {
            U apply = mapper.apply(savedValue.get(), me.get());
            savedValue.set(apply);
            return apply;
        });
        return child;
    }

    /**
     * Create explicit mapping that updates child when parent is updated. Also
     * can decide how to update value based on currently held one. Starting
     * value is null.
     *
     * @param <U>
     * @param mapper
     * @return
     */
    public <U> LazyDependantValue<U> map(BiFunction<U, ? super T, ? extends U> mapper) {
        return map(null, mapper);
    }

}
