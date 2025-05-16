package lt.lb.commons.threads;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Supplier;
import lt.lb.commons.Nulls;
import lt.lb.commons.containers.values.Value;
import lt.lb.uncheckedutils.Checked;
import lt.lb.uncheckedutils.SafeOpt;
import lt.lb.uncheckedutils.func.UncheckedRunnable;

/**
 *
 * @author laim0nas100
 */
public class Futures {

    private static Executor awaitPool = Checked.createDefaultExecutorService();

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
        executeAsync(run, awaitPool);
    }

    public static void awaitAsync(Future future, Executor exe) {
        exe.execute((UncheckedRunnable) () -> future.get());
    }

    public static void awaitAsync(Future future) {
        awaitAsync(future, awaitPool);
    }

    public static SafeOpt runAndAwait(Executor exe, Runnable run) {
        return runAndAwait(exe, run, false);
    }

    public static SafeOpt runAndAwait(Executor exe, Runnable run, boolean interruptable) {
        Nulls.requireNonNulls(exe, run);
        Future fut;
        if (run instanceof Future) {
            fut = (Future) run;
            exe.execute(run);
        } else {
            FutureTask futureTask = new FutureTask(Executors.callable(run));
            fut = futureTask;
            exe.execute(futureTask);
        }

        return await(fut, interruptable);
    }

    public static <T> SafeOpt<T> await(Future<T> future, boolean interruptable) {
        Objects.requireNonNull(future);
        while (true) {
            try {
                return SafeOpt.ofNullable(future.get());
            } catch (InterruptedException ex) {
                if (interruptable) {
                    Thread.currentThread().interrupt();
                    return SafeOpt.error(ex);
                }
            } catch (ExecutionException ex) {
                if (ex.getCause() != null) {
                    return SafeOpt.error(ex.getCause());
                } else {
                    return SafeOpt.error(ex);
                }
            }
        }
    }

    public static <V> MappableFuture<V> mappable(Future<V> future) {
        return (MappableFuture.MappableForwardingFuture<V>) () -> future;
    }

    public static <V> MappableFuture<Iterable<V>> mappableForAll(Iterable<Future<V>> futures) {
        return mappableForAll(futures, awaitPool);
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

     public static <V> FutureTask<V> chainForward(Callable<V> base, Future... next) {
        return chainForward(base, Arrays.asList(next));
    }

    
    public static <V> FutureTask<V> chainForward(Callable<V> base, Collection<Future> next) {
        Objects.requireNonNull(base);
        return ofCallable(() -> {
            V get = base.call();
            for (Future f : next) {
                f.get();
            }
            return get;
        });
    }

    public static <V> FutureTask<V> chainBackward(Callable<V> base, Future... before) {
        return chainBackward(base, Arrays.asList(before));
    }

    public static <V> FutureTask<V> chainBackward(Callable<V> base, Collection<Future> before) {
        Objects.requireNonNull(base);
        return ofCallable(() -> {
            for (Future f : before) {
                f.get();
            }
            return base.call();
        });
    }

    public static <T> CompletableFuture<T> submitAsync(Callable<T> call, Consumer<Throwable> handler, Executor exe) {
        return CompletableFuture.supplyAsync(() -> {
            Value<T> val = new Value<>();
            Checked.uncheckedRunWithHandler(
                    handler,
                    () -> {
                        val.set(call.call());
                    }
            );
            return val.get();
        }, exe);
    }

    public static CompletableFuture<Void> submitAsync(Runnable run, Executor exe) {
        return CompletableFuture.runAsync(run, exe);
    }

    public static CompletableFuture<Void> submitAsync(UncheckedRunnable run, Executor exe) {
        return CompletableFuture.runAsync(run, exe);
    }

    public static void join(Future... futures) {
        join(Arrays.asList(futures));
    }

    public static void join(Collection<Future> futures) {
        futures.forEach(f -> {
            Checked.uncheckedRun(() -> {
                f.get();
            });
        });
    }
}
