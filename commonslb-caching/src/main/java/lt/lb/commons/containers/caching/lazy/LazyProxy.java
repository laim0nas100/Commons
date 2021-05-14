package lt.lb.commons.containers.caching.lazy;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import lt.lb.commons.Java;
import lt.lb.commons.containers.caching.Condition;
import lt.lb.commons.containers.caching.Dependency;
import lt.lb.commons.containers.values.Value;
import lt.lb.commons.containers.values.ValueProxy;
import lt.lb.uncheckedutils.func.UncheckedBiFunction;
import lt.lb.uncheckedutils.func.UncheckedFunction;

/**
 *
 * @author laim0nas100
 */
public interface LazyProxy<T> extends ValueProxy<T>, Dependency<T> {

    public <V> LazyProxy<V> createNew(Dependency<V> supl);

    public void addCondition(Condition con);

    public void addDependency(Dependency dep);

    public default boolean isSynchronized() {
        return true;
    }

    public T syncGet(long now);

    public T unsyncGet(long now);

    public default long getTimestamp() {
        return Java.getNanoTime();
    }

    /**
     * Returns currently present value, ignores all checks and dependencies.
     *
     * @return
     */
    public T fastGet();

    /**
     * Sets value, ignores all checks and dependencies.
     *
     * @param val
     */
    public void fastSet(T val);

    /**
     * Get computed value or init computation and wait for it
     *
     * @return
     */
    @Override
    public default T get() {
        return request(getTimestamp());
    }

    @Override
    public default T request(long now) {
        return isSynchronized() ? syncGet(now) : unsyncGet(now);
    }

    public void syncSet(T val, long now);

    public void unsyncSet(T val, long now);

    @Override
    public default void set(T v) {
        if (isSynchronized()) {
            syncSet(v, getTimestamp());
        } else {
            unsyncSet(v, getTimestamp());
        }
    }

    /**
     * If this is loaded based on given conditions (must satisfy all of them)
     *
     * @return
     */
    public default boolean isLoaded() {
        return isLoadedBefore(getTimestamp());
    }

    /**
     * If this is loaded based on given conditions and given check time (must
     * satisfy all of them)
     *
     * @param now check time returned by {@link LazyProxy#getTimestamp() }
     * @return
     */
    public boolean isLoadedBefore(long now);

    /**
     * Sync dependencies, if there are any.
     *
     * @param now timestamp when the request is happening
     */
    public void syncDependencies(long now);

    /**
     *
     * @return the nano timestamp of this value that was set
     */
    public long getLoaded();

    /**
     * Sets the loaded property with given timestamp
     *
     * @param timestamp
     */
    public void setLoaded(long timestamp);

    /**
     * Sets the loaded property with current timestamp
     */
    public default void setLoaded() {
        setLoaded(getTimestamp());
    }

    /**
     * Invalidates value (needs recomputing)
     */
    public default void invalidate() {
        setLoaded(Long.MAX_VALUE);
    }

    /**
     * Creates a new dependant child that has to be always updated (or
     * reupdated) after this one
     *
     * @param <L>
     * @param child
     * @return
     */
    public default <L extends LazyProxy> L createDependantChild(L child) {
        return createDependantChild(child, false);
    }

    /**
     * Creates a new dependant child that has to be always updated (or
     * reupdated) after this one with parameter which determines whether parent
     * is being called in in child's supplier, so that no repeated parent call
     * must be made
     *
     * @param <L>
     * @param child
     * @param addParentInSupply wether child gets this value as an explicit
     * dependency
     * @return
     */
    public default <L extends LazyProxy> L createDependantChild(L child, boolean addParentInSupply) {
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
    public default <U> LazyProxy<U> map(Function<? super T, ? extends U> mapper) {
        Objects.requireNonNull(mapper);
        LazyProxy<U> createNew = createNew(now -> mapper.apply(this.request(now)));
        return createDependantChild(createNew, false);
    }

    /**
     * Create explicit mapping that updates child when parent is updated
     *
     * @param <U>
     * @param mapper
     * @return
     */
    public default <U> LazyProxy<U> map(UncheckedFunction<? super T, ? extends U> mapper) {
        return map((Function) mapper);
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
    public default <U> LazyProxy<U> map(U startingValue, BiFunction<U, ? super T, ? extends U> mapper) {
        Objects.requireNonNull(mapper);
        Value<LazyProxy<U>> proxyProxy = new Value<>();
        LazyProxy<U> child = map(myVal -> mapper.apply(proxyProxy.get().fastGet(), myVal));
        proxyProxy.set(child); // overcome forward-declaration
        child.fastSet(startingValue);
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
    public default <U> LazyProxy<U> map(BiFunction<U, ? super T, ? extends U> mapper) {
        return map(null, mapper);
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
    public default <U> LazyProxy<U> map(UncheckedBiFunction<U, ? super T, ? extends U> mapper) {
        return map((BiFunction) mapper);
    }

}
