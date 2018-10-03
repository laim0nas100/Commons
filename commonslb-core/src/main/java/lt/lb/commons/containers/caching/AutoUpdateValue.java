/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lt.lb.commons.containers.caching;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import lt.lb.commons.containers.Value;

/**
 *
 * @author laim0nas100
 */
public class AutoUpdateValue<T> extends Value<T> {

    protected volatile AtomicLong called = new AtomicLong(-1);
    protected volatile AtomicLong completed = new AtomicLong(-1);
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

    @Override
    public T get() {

        return this.get(this.forceUpdate);
    }

    public T get(boolean forceUpdate) {
        if (forceUpdate) {
            FutureTask<T> update = this.update();
            try {
                return update.get();
            } catch (InterruptedException | ExecutionException ex) {
                throw new RuntimeException(ex);
            }
        }
        long now = System.nanoTime();

        if (called.get() > now) {
            return super.get();
        }
        if (called.get() <= now) {
            if (completed.get() > now) {
                return super.get();
            } else {
                this.update();
            }
        }
        return super.get();
    }

    public FutureTask<T> createUpdateFunction() {
        AutoUpdateValue<T> me = this;
        FutureTask<T> task = new FutureTask<>(() -> {
            T call = this.cld.call();
            me.set(call);
            return call;
        });
        return task;
    }

    public FutureTask<T> update() {
        long now = System.nanoTime();
        long lastCalled = called.get();
        if (called.compareAndSet(lastCalled, now)) {
            FutureTask<T> updateFunc = this.createUpdateFunction();
            ref.set(updateFunc);
            this.exe.execute(updateFunc);
            return updateFunc;
        }
        return ref.get(); // return last reference
    }

    @Override
    public void set(T val) {
        long now = System.nanoTime();
        long lastSet = completed.get();
        if(lastSet < now && completed.compareAndSet(lastSet, now)){
            super.set(val);
        }
        
    }

    public void setForceUpdate(boolean force) {
        this.forceUpdate = force;
    }
}
