/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lt.lb.commons.threads;

import lt.lb.commons.threads.Promise;
import lt.lb.commons.threads.UnsafeRunnable;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Promise that helps it's dependencies to finish work.
 *
 * @author laim0nas100
 */
public class SharedPromise<Type> extends Promise<Type> {

    public SharedPromise(Callable<Type> clbl) {
        super(clbl);
    }

    public SharedPromise(UnsafeRunnable run) {
        this(() -> {
            run.run();
            return null;
        });
    }

    public SharedPromise() {
        this(() -> null);
    }

    @Override
    public SharedPromise<Type> waitFor(Promise... before) {
        return this.waitFor(Arrays.asList(before));
    }

    @Override
    public SharedPromise<Type> waitFor(Collection<Promise> before) {
        Promise<Type> original = this;
        SharedPromise<Type> newTask = new SharedPromise<>(() -> {
            for (Promise p : before) {
                p.get();
            }
            original.run();

            return original.get();
        });

        return newTask;
    }

    /**
     * @inheritDoc
     */
    @Override
    public Type get() throws InterruptedException, ExecutionException {

        run();
        return super.get();
    }

    /**
     * @inheritDoc
     */
    @Override
    public Type get(long timeout, TimeUnit unit)
            throws InterruptedException, ExecutionException, TimeoutException {
        run();
        return super.get(timeout, unit);
    }

}
