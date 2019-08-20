package lt.lb.commons.containers.caching;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import lt.lb.commons.Java;
import lt.lb.commons.Timer;
import lt.lb.commons.containers.values.Value;
import lt.lb.commons.misc.NestedException;

/**
 *
 * Late updating value, unless forceUpdate = true
 * @author laim0nas100
 */
public class AutoUpdateValue<T> extends Value<T> {

    protected volatile AtomicLong called = new AtomicLong(Long.MIN_VALUE);
    protected volatile AtomicLong lastSetTime = new AtomicLong(Long.MIN_VALUE);
    protected volatile AtomicReference<FutureTask<T>> ref = new AtomicReference();
    protected Callable<T> cld;
    protected boolean forceUpdate;
    protected Executor exe;

    public AutoUpdateValue(T current, Callable<T> clb, Executor exe, boolean forceUpdate) {
        super(current);
        this.cld = clb;
        this.forceUpdate = forceUpdate;
        this.exe = exe;

    }

    /**
     * Gets last read value, and updates in case there has been changes
     * @return 
     */
    @Override
    public T get() {
        return this.get(this.forceUpdate);
    }

    
    /**
     * Gets explicitly updated mode last read value 
     * @param forceUpdate
     * @return 
     */
    public T get(boolean forceUpdate) {
        if (forceUpdate) {
            Future<T> update = this.update();
            try {
                return update.get();
            } catch (InterruptedException | ExecutionException ex) {
                throw NestedException.of(ex);
            }
        }
        long now = Java.getNanoTime();

        if (called.get() > now) {
            return super.get();
        }
        if (called.get() <= now) {
            if (lastSetTime.get() > now) {
                return super.get();
            } else {
                this.update();
            }
        }
        return super.get();
    }
    
    /**
     * Get computed value and init new computation
     * @return 
     */
    public T getAndUpdate(){
        return this.get(false);
    }
    
    /**
     * Init new computation, wait until it finishes, return new updated value
     * @return 
     */
    public T updateAndGet(){
        return this.get(true);
    }

    /**
     * 
     * @return update task, that you can execute at your own volition and set new value afterwards
     */
    private FutureTask<T> createUpdateFunction() {
        AutoUpdateValue<T> me = this;
        FutureTask<T> task = new FutureTask<>(() -> {
            T call = this.cld.call();
            me.set(call);
            return call;
        });
        return task;
    }

    /**
     * updates value in background
     * @return Future so you can monitor when update is finished
     */
    public Future<T> update() {
        long now = Java.getNanoTime();
        long lastCalled = called.get();
        if (called.compareAndSet(lastCalled, now)) {
            FutureTask<T> updateFunc = this.createUpdateFunction();
            ref.set(updateFunc);
            this.exe.execute(updateFunc);
            return updateFunc;
        }
        return ref.get(); // return last reference
    }

    /**
     * Sets new value, updates completed time
     * @param val 
     */
    @Override
    public void set(T val) {
        long now = Java.getNanoTime();
        long lastSet = lastSetTime.get();
        if(lastSet < now && lastSetTime.compareAndSet(lastSet, now)){
            super.set(val);
        }
        
    }

    /**
     * Change update policy
     * @param force 
     */
    public void setForceUpdate(boolean force) {
        this.forceUpdate = force;
    }
}
