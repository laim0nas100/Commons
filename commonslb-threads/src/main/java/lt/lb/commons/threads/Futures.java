package lt.lb.commons.threads;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;
import lt.lb.uncheckedutils.func.UncheckedRunnable;

/**
 *
 * @author laim0nas100
 */
public class Futures {

    public static class DoneFuture<T> implements Future<T> {

        protected final boolean done;
        protected final T res;

        public DoneFuture(boolean done, T res) {
            this.done = done;
            this.res = res;
        }

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            return false;
        }

        @Override
        public boolean isCancelled() {
            return false;
        }

        @Override
        public boolean isDone() {
            return done;
        }

        @Override
        public T get() {
            return res;
        }

        @Override
        public T get(long timeout, TimeUnit unit) {
            return res;
        }
    }

    public static class ThrowingFuture<T> implements Future<T> {

        protected Supplier<? extends Throwable> exception;

        public ThrowingFuture(Supplier<? extends Throwable> exception) {
            this.exception = Objects.requireNonNull(exception);
        }

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            return false;
        }

        @Override
        public boolean isCancelled() {
            return false;
        }

        @Override
        public boolean isDone() {
            return true;
        }

        @Override
        public T get() throws ExecutionException {
            throw new ExecutionException(exception.get());
        }

        @Override
        public T get(long timeout, TimeUnit unit) throws ExecutionException {
            throw new ExecutionException(exception.get());
        }
    }

    public static final Future<?> emptyDone = new DoneFuture<>(true, null);

    public static <V> Future<V> done(V result) {
        return new DoneFuture<>(true, result);
    }

    public static <V> Future<V> exceptional(Supplier<? extends Throwable> exceptionSupply) {
        return new ThrowingFuture<>(exceptionSupply);
    }

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
        exe.execute((UncheckedRunnable) () -> future.get());
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
