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
        mapped.awaitAsync(exe);
        return mapped;
    }

    public default <R> MappableFuture<R> flatMapEager(Executor exe, UncheckedFunction< ? super T, ? extends Future<? extends R>> func) {
        MappableFuture<R> mapped = flatMap(func);
        mapped.awaitAsync(exe);
        return mapped;
    }

    public default <R> MappableFuture<R> flatMapEager(UncheckedFunction< ? super T, ? extends Future<? extends R>> func) {
        return flatMapEager(getDefaultExecutor(), func);
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

    /**
     * Await given time and get result or exception boxed in a {@link SafeOpt}
     *
     * @return
     */
    public default SafeOpt<T> safeGet(long timeout, TimeUnit unit) {
        return SafeOpt.ofGet((UncheckedSupplier<T>) () -> get(timeout, unit));
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
                    return atsBasic.waitedRetrieve(compl, () -> {
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
                return ats.waitedRetrieve(compl,  () -> {
                    long toWait = nanos - (System.nanoTime() - calledAt);
                    T unmapped = me.get(toWait, TimeUnit.NANOSECONDS);
                    R mapped = func.applyUnchecked(unmapped);
                    return mapped;
                });
            }
        };

    }

    public default <R> MappableFuture<R> flatMap(UncheckedFunction< ? super T, ? extends Future<? extends R>> func) {
        Objects.requireNonNull(func);
        MappableFuture<? extends Future<? extends R>> map = this.map(func);
        return new MappableFuture<R>() {
            @Override
            public boolean cancel(boolean mayInterruptIfRunning) {
                if (!map.isDone()) {
                    return map.cancel(mayInterruptIfRunning);
                } else {
                    return map.safeGet().map(m -> m.cancel(mayInterruptIfRunning)).orElse(false);
                }
            }

            @Override
            public boolean isCancelled() {
                if (map.isCancelled()) {
                    return true;
                }
                if (map.isDone()) {
                    return SafeOpt.ofGet(() -> map.get()).filter(f -> f.isCancelled()).isPresent();
                } else {
                    return false;
                }
            }

            @Override
            public boolean isDone() {
                if (map.isDone()) {
                    return SafeOpt.ofGet(() -> map.get()).filter(f -> f.isDone()).isPresent();
                } else {
                    return false;
                }
            }

            @Override
            public R get() throws InterruptedException, ExecutionException {
                return map.get().get();
            }

            @Override
            public R get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
                if(map.isDone()){
                    return map.get().get(timeout, unit);
                }
                long nanoTime = System.nanoTime();
                Future<? extends R> get = map.get(timeout, unit);
                long afterGet = System.nanoTime();
                long nanoUsed = afterGet - nanoTime;

                long nanosLeft = TimeUnit.NANOSECONDS.convert(timeout, unit) - nanoUsed;

                return get.get(nanosLeft, TimeUnit.NANOSECONDS);
            }

        };
    }

}
