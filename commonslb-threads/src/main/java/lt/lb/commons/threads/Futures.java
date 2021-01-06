package lt.lb.commons.threads;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.function.Supplier;
import lt.lb.commons.func.unchecked.UnsafeRunnable;

/**
 *
 * @author laim0nas100
 */
public class Futures {

    public static <V> FutureTask<V> ofCallable(Callable<V> call) {
        return new FutureTask<>(call);
    }
    
    public static <V> FutureTask<V> ofSupplier(Supplier<V> call) {
        return new FutureTask<>(call::get);
    }

    public static FutureTask<Void> ofRunnable(Runnable r) {
        return new FutureTask<>(() -> {
            r.run();
            return null;
        });
    }

    public static void executeAsync(Runnable run, Executor exe) {
        exe.execute(run);
    }

    public static void executeAsync(Runnable run) {
        executeAsync(run, ForkJoinPool.commonPool());
    }

    public static void awaitAsync(Future future, Executor exe) {
        exe.execute((UnsafeRunnable) () -> future.get());
    }

    public static void awaitAsync(Future future) {
        awaitAsync(future, ForkJoinPool.commonPool());
    }

    public static <V> MappableFuture<V> mappable(Future<V> future) {
        return (MappableFuture.MappableForwardingFuture<V>) () -> future;
    }

    public static <V> MappableFuture<Iterable<V>> mappableForAll(Iterable<Future<V>> futures) {
        return mappableForAll(futures, ForkJoinPool.commonPool());
    }

    public static <V> MappableFuture<Iterable<V>> mappableForAll(Iterable<Future<V>> futures, Executor exe) {
        FutureTask<Iterable<V>> task = new FutureTask<>(() -> {
            ArrayList<V> list = new ArrayList<>();
            for (Future<V> f : futures) {
                list.add(f.get());
            }
            return list;
        });

        exe.execute(task);
        return mappable(task);
    }

    public static <V> FutureTask<V> empty() {
        return ofCallable(() -> null);
    }

    public static <V> FutureTask<V> chainForward(Future<V> base, Collection<Future> next) {
        return ofCallable(() -> {
            V get = base.get();
            for (Future f : next) {
                f.get();
            }
            return get;
        });
    }

    public static <V> FutureTask<V> chainBackward(Future<V> base, Collection<Future> before) {
        return ofCallable(() -> {
            for (Future f : before) {
                f.get();
            }
            return base.get();
        });
    }
}
