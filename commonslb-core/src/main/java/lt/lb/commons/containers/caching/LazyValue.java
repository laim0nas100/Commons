package lt.lb.commons.containers.caching;

import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;
import lt.lb.commons.F;
import lt.lb.commons.Java;
import lt.lb.commons.Timer;
import lt.lb.commons.containers.values.Value;
import lt.lb.commons.threads.Futures;

/**
 * Value that loads after being called. Can manually set it.
 *
 * @author laim0nas100
 * @param <T> type
 */
public class LazyValue<T> extends Value<T> {

    protected Long loaded = null;
    protected Supplier<Boolean> loader = () -> loaded != null && loaded <= Java.getNanoTime();
    protected Supplier<T> supply;

    protected AtomicBoolean inGet = new AtomicBoolean(false);
    protected volatile Future<T> future;

    public LazyValue(Supplier<T> supply) {
        this.supply = supply;
    }

    public LazyValue(T value) {
        this(() -> value);
    }

    /**
     * Set value explicitly, update time
     *
     * @param val
     */
    @Override
    public void set(T val) {
        loaded = Java.getNanoTime();
        super.set(val);
    }

    /**
     * Get computed value or init computation and wait for it
     *
     * @return
     */
    @Override
    public T get() {
        if (!loader.get() || future == null) {
            if (inGet.compareAndSet(false, true)) {
                FutureTask<T> fut = Futures.ofSupplier(() -> {
                    return super.setAndGet(supply);
                });
                fut.run();
                future = fut;

                inGet.set(false);
                F.checkedRun(() -> {
                    future.get();
                });

            } else {
                return F.unsafeCall(() -> future.get());
            }
        }

        return super.get();
    }

    /**
     * Invalidates value (needs recomputing)
     */
    public void invalidate() {
        loaded = null;
    }

}
