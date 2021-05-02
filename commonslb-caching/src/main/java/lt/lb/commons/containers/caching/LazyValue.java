package lt.lb.commons.containers.caching;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.function.Supplier;
import lt.lb.commons.Java;
import lt.lb.commons.containers.values.LongValue;
import lt.lb.commons.containers.values.Value;

/**
 * Value that loads after being called. Can manually set it.
 *
 * @author laim0nas100
 * @param <T> type
 */
public class LazyValue<T> extends Value<T> {
    //should be good for a really long time

    protected LongValue loaded = new LongValue(Long.MAX_VALUE);
    protected Deque<Condition> conditions = new ArrayDeque<>();
    protected Supplier<? extends T> supply;
    protected Deque<Supplier> dependants;

    public LazyValue(Supplier<? extends T> supply) {
        this.supply = supply;
        conditions.add(now -> now > loaded.get());

    }

    public LazyValue(T value) {
        this(() -> value);
    }

    public void addCondition(Condition con) {
        conditions.add(con);
    }

    public void addDependency(Supplier dep) {
        if (dependants == null) {
            dependants = new ArrayDeque<>(1);
        }
        dependants.add(dep);
    }

    /**
     * Set value explicitly, update time
     *
     * @param val
     */
    @Override
    public void set(T val) {
        super.set(val);
        loaded.set(Java.getNanoTime());
    }

    /**
     * Get computed value or init computation and wait for it
     *
     * @return
     */
    @Override
    public T get() {
        long now = Java.getNanoTime();
        if (!isLoadedBefore(now)) {
            return syncGet(now);
        }
        return super.get();
    }

    /**
     * If this LazyValue is loaded based on given conditions (must satisfy all
     * of them)
     *
     * @return
     */
    public boolean isLoaded() {
        return isLoadedBefore(Java.getNanoTime());
    }

    /**
     * If this LazyValue is loaded based on given conditions and given check
     * time (must satisfy all of them)
     *
     * @param now check time returned by {@link Java#getNanoTime() }
     * @return
     */
    public boolean isLoadedBefore(long now) {
        for (Condition check : conditions) {
            if (check.isFalse(now)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Sync dependencies, if there are any.
     */
    public void syncDependencies() {
        if (dependants == null) {
            return;
        }
        for (Supplier sup : dependants) {
            sup.get();
        }
    }

    /**
     *
     * @return the nano timestamp of this value that was set
     */
    public long getLoaded() {
        return loaded.get();
    }

    protected synchronized T syncGet(long now) {
        if (!isLoadedBefore(now)) {
            syncDependencies();
            return super.setAndGetSupl(supply);
        }

        return super.get();
    }

    /**
     * Invalidates value (needs recomputing)
     */
    public void invalidate() {
        loaded.set(Long.MAX_VALUE);
    }

}
