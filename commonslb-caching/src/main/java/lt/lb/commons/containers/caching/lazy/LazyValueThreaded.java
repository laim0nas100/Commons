package lt.lb.commons.containers.caching.lazy;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import lt.lb.commons.containers.caching.Condition;
import lt.lb.commons.containers.caching.Dependency;

/**
 *
 * @author laim0nas100
 */
public class LazyValueThreaded<T> extends LazyValue<T> {

    protected ThreadLocal<LazyValue<T>> threadLocal;

    public void collectThreadLocal(Consumer<ThreadLocal> cons) {
        cons.accept(threadLocal);
    }

    public LazyValueThreaded(Supplier<? extends T> supply) {
        this(Dependency.of(supply));
    }

    public LazyValueThreaded(Dependency<? extends T> supply) {
        super(supply, false);
        threadLocal = ThreadLocal.withInitial(() -> {
            return new LazyValue<>(this.supply, this.conditions, this.dependants, false);
        });
    }

    protected LazyValueThreaded(Dependency<? extends T> supply, List<Condition> conditions, List<Dependency> dependents, boolean sync) {
        super(supply, conditions, dependents, sync);
    }

    public LazyValueThreaded(T value) {
        this(() -> value);
    }

    @Override
    public void set(T val) {
        threadLocal.get().set(val);
    }

    @Override
    public T get() {
        return threadLocal.get().get();
    }

    @Override
    public T request(long timestamp) {
        return threadLocal.get().request(timestamp);
    }

    @Override
    public boolean isLoaded() {
        return threadLocal.get().isLoaded();
    }

    @Override
    public boolean isLoadedBefore(long now) {
        return threadLocal.get().isLoadedBefore(now);
    }

    @Override
    public void syncDependencies(long now) {
        threadLocal.get().syncDependencies(now);
    }

    @Override
    public long getLoaded() {
        return threadLocal.get().getLoaded();
    }

    @Override
    public T syncGet(long now) {
        return unsyncGet(now);
    }

    @Override
    public T unsyncGet(long now) {
        return threadLocal.get().unsyncGet(now);
    }

    @Override
    public void invalidate() {
        threadLocal.get().invalidate();
    }

    @Override
    public void unsyncSet(T val, long now) {
        threadLocal.get().unsyncSet(val, now);
    }

    @Override
    public void syncSet(T val, long now) {
        unsyncSet(val, now);
    }

    @Override
    public void setLoaded(long timestamp) {
        threadLocal.get().setLoaded(timestamp);
    }

    @Override
    public boolean isSynchronized() {
        return false;
    }

    @Override
    public void fastSet(T val) {
        threadLocal.get().fastSet(val);
    }

    @Override
    public T fastGet() {
        return threadLocal.get().fastGet();
    }

    @Override
    public <V> LazyProxy<V> createNew(Dependency<V> dep) {
        return new LazyValueThreaded<>(dep);
    }

}
