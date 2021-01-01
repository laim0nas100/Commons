package lt.lb.commons.threads;

import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.function.Supplier;
import lt.lb.commons.func.unchecked.UnsafeRunnable;
import lt.lb.commons.func.unchecked.UnsafeSupplier;

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

    public static void awaitAsync(Future future, Executor exe) {
        exe.execute((UnsafeRunnable) () -> future.get());
    }

    public static void awaitAsync(Future future) {
        awaitAsync(future, ForkJoinPool.commonPool());
    }

    public static <V, R> Future<R> mapped(Future<V> future, Function<? super V, ? extends R> func) {
        CompletableFuture<R> compl = new CompletableFuture();
        AtomicBoolean mapping = new AtomicBoolean(false);
        return new Future<R>() {
            @Override
            public boolean cancel(boolean mayInterruptIfRunning) {
                return future.cancel(mayInterruptIfRunning);
            }

            @Override
            public boolean isCancelled() {
                return future.isCancelled();
            }

            @Override
            public boolean isDone() {
                return future.isDone();
            }

            private void complete(UnsafeSupplier<V> supl) {
                try {
                    compl.complete(func.apply(supl.unsafeGet()));
                } catch (Throwable th) {
                    mapping.compareAndSet(true, false);
                    compl.completeExceptionally(th);
                }
            }

            @Override
            public R get() throws InterruptedException, ExecutionException {

                if (mapping.compareAndSet(false, true)) {
                    complete(() -> future.get());
                }

                return compl.get();
            }

            @Override
            public R get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
                if (mapping.compareAndSet(false, true)) {
                    complete(() -> future.get(timeout, unit));
                }

                return compl.get();
            }
        };

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
