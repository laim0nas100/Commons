package lt.lb.commons.containers.caching.lazy;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import lt.lb.commons.Java;
import lt.lb.commons.containers.caching.Condition;
import lt.lb.commons.containers.caching.Dependency;
import lt.lb.commons.containers.values.LongValue;

/**
 * Value that loads after being called. Can manually set it.
 *
 * @author laim0nas100
 * @param <T> type
 */
public class LazyValue<T> implements LazyProxy<T> {
    //should be good for a really long time

    protected T val;
    protected LongValue loaded = new LongValue(Long.MAX_VALUE);
    protected List<Condition> conditions;
    protected Dependency<? extends T> supply;
    protected List<Dependency> dependants;

    protected boolean sync;

    public LazyValue(Supplier<? extends T> supply) {
        this(Dependency.of(supply), false);
    }

    public LazyValue(Supplier<? extends T> supply, boolean sync) {
        this(Dependency.of(supply), sync);
    }

    public LazyValue(Dependency<? extends T> supply, boolean sync) {
        this.supply = supply;
        this.conditions = new ArrayList<>(1);
        conditions.add(now -> now > loaded.get());
        this.sync = sync;
    }

    public LazyValue(T value) {
        this(now -> value, false);
    }

    protected LazyValue(Dependency<? extends T> supply, List<Condition> conditions, List<Dependency> dependents, boolean sync) {
        this.supply = supply;
        this.conditions = conditions == null ? null : new ArrayList<>(conditions);
        this.dependants = dependents == null ? null : new ArrayList<>(dependents);
        this.sync = sync;
    }

    @Override
    public void addCondition(Condition con) {
        if (conditions == null) {
            conditions = new ArrayList<>(1);
        }
        conditions.add(con);
    }

    @Override
    public void addDependency(Dependency dep) {
        if (dependants == null) {
            dependants = new ArrayList<>(1);
        }
        dependants.add(dep);
    }

    @Override
    public boolean isSynchronized() {
        return sync;
    }

    /**
     * If this LazyValue is loaded based on given conditions (must satisfy all
     * of them)
     *
     * @return
     */
    @Override
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
    @Override
    public boolean isLoadedBefore(long now) {
        if (conditions == null) {
            return true;
        }
        for (Condition check : conditions) {
            if (check.isFalse(now)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Sync dependencies, if there are any.
     *
     * @param now
     */
    @Override
    public void syncDependencies(long now) {
        if (dependants == null) {
            return;
        }
        for (Dependency dep : dependants) {
            dep.request(now);
        }
    }

    /**
     *
     * @return the nano timestamp of this value that was set
     */
    @Override
    public long getLoaded() {
        return loaded.get();
    }

    @Override
    public void setLoaded(long timestamp) {
        loaded.set(timestamp);
    }

    @Override
    public synchronized T syncGet(long now) {
        return unsyncGet(now);
    }

    @Override
    public T unsyncGet(long now) {
        if (!isLoadedBefore(now)) {
            syncDependencies(now);
            set(supply.request(now));
        }

        return fastGet();
    }

    @Override
    public synchronized void syncSet(T val, long now) {
        unsyncSet(val, now);
    }

    @Override
    public void unsyncSet(T val, long now) {
        fastSet(val);
        setLoaded(now);
    }

    @Override
    public <V> LazyProxy<V> createNew(Dependency<V> supl) {
        return new LazyValue<>(supl, isSynchronized());
    }

    @Override
    public T fastGet() {
        return val;
    }

    @Override
    public void fastSet(T val) {
        this.val = val;
    }

}
