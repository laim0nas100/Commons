package lt.lb.commons.containers.caching;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import lt.lb.commons.containers.values.ValueProxy;
import lt.lb.commons.threads.sync.SynchronizedValueSupplier;
import lt.lb.uncheckedutils.NestedException;

/**
 *
 * Late updating value, unless forceUpdate = true
 *
 * @author laim0nas100
 */
public class AutoUpdateValue<T> implements ValueProxy<T> {

    protected boolean forceUpdate;
    protected SynchronizedValueSupplier<T> suppl;

    public AutoUpdateValue(T current, Callable<T> clb, Executor exe, boolean forceUpdate) {
        this.forceUpdate = forceUpdate;
        this.suppl = new SynchronizedValueSupplier<>(current, () -> {
            T call = clb.call();
            this.set(call);
            return call;
        }, exe);

    }

    /**
     * Gets last read value, and updates in case there has been changes
     *
     * @return
     */
    @Override
    public T get() {
        return this.get(this.forceUpdate);
    }

    /**
     * Gets explicitly updated mode last read value
     *
     * @param forceUpdate
     * @return
     */
    public T get(boolean forceUpdate) {

        try {
            if (forceUpdate) {

                return suppl.getUpdate();

            } else {
                T get = this.suppl.get();
                this.suppl.execute();
                return get;

            }
        } catch (InterruptedException | ExecutionException ex) {
            throw NestedException.of(ex);
        }

    }

    /**
     * Get computed value and init new computation
     *
     * @return
     */
    public T getAndUpdate() {
        return this.get(false);
    }

    /**
     * Init new computation, wait until it finishes, return new updated value
     *
     * @return
     */
    public T updateAndGet() {
        return this.get(true);
    }

    /**
     * updates value in background
     *
     * @return Future so you can monitor when update is finished
     */
    public Future<T> update() {
        return this.suppl.execute();
    }

    /**
     * Sets new value, updates completed time
     *
     * @param val
     */
    @Override
    public void set(T val) {
        this.suppl.set(val);

    }

    /**
     * Change update policy
     *
     * @param force
     */
    public void setForceUpdate(boolean force) {
        this.forceUpdate = force;
    }
}
