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
import lt.lb.commons.func.unchecked.UnsafeFunction;
import lt.lb.commons.func.unchecked.UnsafeRunnable;

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

    public default MappableFuture<T> awaitAsync(Executor exe) {
        exe.execute((UnsafeRunnable) () -> this.get());
        return this;
    }

    public default MappableFuture<T> awaitAsync() {
        awaitAsync(ForkJoinPool.commonPool());
        return this;
    }

    public default <R> MappableFuture<R> mapEager(UnsafeFunction<? super T, ? extends R> func) {
        return mapEager(ForkJoinPool.commonPool(), func);
    }

    public default <R> MappableFuture<R> mapEager(Executor exe, UnsafeFunction<? super T, ? extends R> func) {
        MappableFuture<R> mapped = map(func);
        awaitAsync(exe);
        return mapped;
    }

    public default <R> MappableFuture<R> map(UnsafeFunction<? super T, ? extends R> func) {
        Objects.requireNonNull(func);
        CompletableFuture<R> compl = new CompletableFuture();
        MappableFuture<T> me = this;
        // in case we recieve multiple 'get' calls, but only one can actually do work, so
        ArrayBlockingQueue q = new ArrayBlockingQueue(1, true);
        final Object dummy = new Object();
        q.add(dummy);
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
                if (compl.isDone()) { //allready mapped, just get
                    return compl.get();
                }
                Object poll = q.take();
                if (poll == null) { // should never happen
                    if (compl.isDone()) { //allready mapped while we were waiting, just get
                        return compl.get();
                    }
                    throw new IllegalStateException("Interrupted an uniterruptable wait.");
                } else { // we are mapping
                    if (compl.isDone()) { //allready mapped while we were waiting, just get
                        q.offer(poll);// rescue other waiters
                        return compl.get();
                    }
                    try {
                        T unmapped = me.get();

                        try {
                            R mapped = func.applyUnsafe(unmapped);
                            compl.complete(mapped);
                        } catch (Throwable th) { // mapping related exception, same as exception exception
                            compl.completeExceptionally(th);
                        }

                    } catch (InterruptedException ex) {

                        q.offer(poll);// non execution related exception, just pass is through and give a chance another thread to map.
                        throw ex;
                    } catch (ExecutionException ex) { // execution related exception
                        compl.completeExceptionally(ex);
                    }

                    q.offer(poll);// rescue other waiters
                }

                return compl.get();
            }

            @Override
            public R get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
                if (compl.isDone()) { //allready mapped, just get
                    return compl.get();
                }
                Object poll = q.poll(timeout, unit);
                if (poll == null) { // timed out
                    if (compl.isDone()) { //allready mapped while we were waiting, just get
                        return compl.get();
                    }
                    throw new TimeoutException("Timeout");
                } else { // we are mapping
                    if (compl.isDone()) { //allready mapped while we were waiting, just get
                        q.offer(poll);// rescue other waiters
                        return compl.get();
                    }
                    try {
                        T unmapped = me.get(timeout, unit);

                        try {
                            R mapped = func.applyUnsafe(unmapped);
                            compl.complete(mapped);
                        } catch (Throwable th) { // mapping related exception, same as exception exception
                            compl.completeExceptionally(th);
                        }
                    } catch (TimeoutException | InterruptedException ex) {

                        q.offer(poll);// non execution related exception, just pass is through and give a chance another thread to map.
                        throw ex;
                    } catch (Throwable ex) { // execution related exception
                        compl.completeExceptionally(ex);
                    }

                    q.offer(poll);// rescue other waiters
                }

                return compl.get();
            }
        };

    }

}
