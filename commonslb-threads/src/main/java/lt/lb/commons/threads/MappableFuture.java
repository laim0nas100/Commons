package lt.lb.commons.threads;

import java.util.Objects;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import lt.lb.commons.F;
import lt.lb.commons.SafeOpt;
import lt.lb.commons.misc.NestedException;
import lt.lb.commons.func.unchecked.UncheckedFunction;
import lt.lb.commons.func.unchecked.UncheckedRunnable;
import lt.lb.commons.func.unchecked.UncheckedSupplier;

/**
 * Mappable future. Easier and less cluttered version of CompletableFuture.
 * Disregards failure paths, because usually they are irrelevant. If failure
 * happens, just handle exception and disregard left-over execution path.
 * Mapping is lazy by default (if get is never called, why bother doing the work
 * of mapping the data?).
 *
 * Eager mapping behavior is achieved by feeding the {@link Future#get() }
 * operation into the one of executors. By default it is the common ForkJoin
 * pool.
 *
 * @author laim0nas100
 */
public interface MappableFuture<T> extends Future<T> {

    public interface MappableForwardingFuture<T> extends MappableFuture<T>, ForwardingFuture<T> {
    }

    public default Executor getDefaultExecutor() {
        return ForkJoinPool.commonPool();
    }

    public default MappableFuture<T> awaitAsync(Executor exe) {
        exe.execute((UncheckedRunnable) () -> this.get());
        return this;
    }

    public default MappableFuture<T> awaitAsync() {
        awaitAsync(getDefaultExecutor());
        return this;
    }

    public default <R> MappableFuture<R> mapEager(UncheckedFunction<? super T, ? extends R> func) {
        return mapEager(getDefaultExecutor(), func);
    }

    public default <R> MappableFuture<R> mapEager(Executor exe, UncheckedFunction<? super T, ? extends R> func) {
        MappableFuture<R> mapped = map(func);
        awaitAsync(exe);
        return mapped;
    }

    /**
     * Await and get result masking exceptions in
     * {@link lt.lb.commons.misc.NestedException}
     *
     * @return
     */
    public default T justGet() throws NestedException {
        return F.uncheckedCall(() -> get());
    }

    /**
     * Await and get result or exception boxed in a {@link SafeOpt}
     *
     * @return
     */
    public default SafeOpt<T> safeGet() {
        return SafeOpt.ofGet((UncheckedSupplier<T>) () -> get());
    }

    public default <R> MappableFuture<R> map(UncheckedFunction<? super T, ? extends R> func) {
        Objects.requireNonNull(func);
        CompletableFuture<R> compl = new CompletableFuture();
        MappableFuture<T> me = this;
        // in case we recieve multiple 'get' calls before mapping is done, but only one can actually do the work
        ArrayBlockingQueue q = new ArrayBlockingQueue(1, true);
        final Object dummy = new Object();
        q.add(dummy);

        final AsyncUtil.AsyncTokenSupport atsBasic = new AsyncUtil.AsyncTokenSupport() {
            @Override
            public boolean getToken() throws InterruptedException, TimeoutException {
                return q.take() != null;
            }

            @Override
            public boolean returnToken() throws InterruptedException, TimeoutException {
                return q.offer(dummy);
            }
        };

        return new MappableFuture<R>() {
            @Override
            public boolean cancel(boolean mayInterruptIfRunning) {
                return me.cancel(mayInterruptIfRunning);
            }

            @Override
            public boolean isCancelled() {
                return me.isCancelled();
            }

            @Override
            public boolean isDone() {
                return me.isDone();
            }

            @Override
            public R get() throws InterruptedException, ExecutionException {

                try {
                    return AsyncUtil.waitedRetrieve(compl, atsBasic, () -> {
                        return func.applyUnchecked(me.get());
                    });
                } catch (TimeoutException timeout) {
                    throw new IllegalStateException("Impossible timeout", timeout);
                }
            }

            @Override
            public R get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
                final long nanos = unit.toNanos(timeout);

                AsyncUtil.AsyncTokenSupport ats = new AsyncUtil.AsyncTokenSupport() {
                    @Override
                    public boolean getToken() throws InterruptedException, TimeoutException {
                        return q.poll(nanos, TimeUnit.NANOSECONDS) != null;
                    }

                    @Override
                    public boolean returnToken() throws InterruptedException, TimeoutException {
                        return q.offer(dummy);
                    }
                };

                final long calledAt = System.nanoTime();
                //waiting for mapping queue and waiting for value should share the same time
                return AsyncUtil.waitedRetrieve(compl, ats, () -> {
                    long toWait = nanos - (System.nanoTime() - calledAt);
                    T unmapped = me.get(toWait, TimeUnit.NANOSECONDS);
                    R mapped = func.applyUnchecked(unmapped);
                    return mapped;
                });
            }
        };

    }

}
