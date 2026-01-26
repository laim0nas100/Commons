package lt.lb.commons.containers.caching.lazy;

import java.util.List;
import java.util.function.Consumer;
import lt.lb.commons.F;
import lt.lb.commons.containers.caching.Condition;
import lt.lb.commons.containers.caching.Dependency;
import lt.lb.commons.containers.values.ThreadLocalValue;

/**
 *
 * @author laim0nas100
 */
public class LazySettableValueThreaded<T> extends LazyValueThreaded<T> {
    
    protected final ThreadLocalValue<T> settable;
    
    @Override
    public void collectThreadLocal(Consumer<ThreadLocal> cons) {
        cons.accept(settable);
        cons.accept(threadLocal);
    }
    
    public LazySettableValueThreaded(T value) {
        super(new ThreadLocalValue<>(value));
        WrappedDep<T, ThreadLocalValue<T>> wrapped = F.cast(supply);
        this.settable = wrapped.implementation;
    }
    
    public LazySettableValueThreaded() {
        this(null);
    }
    
    protected LazySettableValueThreaded(Dependency<? extends T> supply, List<Condition> conditions, List<Dependency> dependents, boolean sync) {
        super(supply, conditions, dependents, sync);
        WrappedDep<T, ThreadLocalValue<T>> wrapped = F.cast(supply);
        this.settable = wrapped.implementation;
    }
    
    @Override
    public void fastSet(T val) {
        super.fastSet(settable.setAndGet(val));
    }
    
}
