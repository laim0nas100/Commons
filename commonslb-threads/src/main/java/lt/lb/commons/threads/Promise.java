/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lt.lb.commons.threads;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.*;

/**
 *
 * @author Lemmin
 * @param <Type> return type
 */
public class Promise<Type> extends FutureTask<Type> {

    public Promise(Callable<Type> clbl) {
        super(clbl);
    }

    public Promise(UnsafeRunnable run) {
        this(() -> {
            run.run();
            return null;
        });
    }

    public Promise() {
        this(() -> null);
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

    public Promise<Type> waitFor(Promise... before) {
        return this.waitFor(Arrays.asList(before));
    }

    public Promise<Type> waitFor(Collection<Promise> before) {
        Promise<Type> original = this;
        Promise<Type> newTask = new Promise<>(() -> {
            for (Promise p : before) {
                p.get();
            }
            original.run();

            return original.get();
        });

        return newTask;
    }

    public Promise<Type> execute(Executor e) {
        e.execute(this);
        return this;
    }

    public Promise<Type> collect(Collection<Promise> collection) {
        collection.add(this);
        return this;
    }

}
