package lt.lb.commons.threads;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.*;
import lt.lb.commons.func.unchecked.UncheckedFunction;
import lt.lb.commons.func.unchecked.UncheckedRunnable;

/**
 *
 * @author laim0nas100
 * @param <Type> return type
 */
public class Promise<Type> extends FutureTask<Type> implements MappableFuture<Type> {

    public Promise(Future<Type> future) {
        this(() -> future.get());
    }

    public Promise(Callable<Type> clbl) {
        super(clbl);
    }

    public Promise(UncheckedRunnable run) {
        this(() -> {
            run.run();
            return null;
        });
    }

    public Promise(Collection<Promise> before) {
        this(() -> {
            for (Promise p : before) {// look for a thing to run
                p.run();
            }
            for (Promise p : before) {// await all
                p.get();
            }
            return null;
        });
    }

    public Promise() {
        this(() -> null);
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

    public Promise<Type> waitForAndRun(Collection<Promise> before) {
        Promise<Type> original = this;
        Promise<Type> newTask = new Promise<>(() -> {
            for (Promise p : before) {// look for a thing to run
                p.run();
            }
            for (Promise p : before) {// await all
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

    public Promise<Type> collect(Collection<Promise<Type>> collection) {
        collection.add(this);
        return this;
    }

    @Override
    public <R> Promise<R> map(UncheckedFunction<? super Type, ? extends R> func) {
        return new Promise(MappableFuture.super.map(func));
    }

    @Override
    public <R> Promise<R> mapEager(Executor exe, UncheckedFunction<? super Type, ? extends R> func) {
        return new Promise(MappableFuture.super.mapEager(exe, func)); 
    }

    @Override
    public <R> Promise<R> mapEager(UncheckedFunction<? super Type, ? extends R> func) {
        return new Promise(MappableFuture.super.mapEager(func)); 
    }

    @Override
    public Promise<Type> awaitAsync() {
        MappableFuture.super.awaitAsync();
        return this;
    }

    @Override
    public Promise<Type> awaitAsync(Executor exe) {
        MappableFuture.super.awaitAsync(exe);
        return this;
    }

}
