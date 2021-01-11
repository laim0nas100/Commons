package lt.lb.commons.threads.sync;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.LockSupport;

/**
 *
 * @author laim0nas100
 */
public class SynchronizedValueSupplier<T> {

    private AtomicLong counter = new AtomicLong(Long.MIN_VALUE);
    private Callable<T> call;
    private Executor executor;

    private AtomicReference<TimeAwareFutureTask<T>> future = new AtomicReference();

    public SynchronizedValueSupplier(T initial, Callable<T> call, Executor executor) {
        this.call = call;
        this.executor = executor;
        future.set(new TimeAwareFutureTask<>(() -> initial, this::inc, Long.MIN_VALUE));
        future.get().run();
    }

    private long inc() {
        return counter.getAndIncrement();
    }

    /**
     * Manually execute update
     *
     * @return
     */
    public Future<T> execute() {
        TimeAwareFutureTask<T> prev = future.get();
        TimeAwareFutureTask<T> timeAwareFutureTask = new TimeAwareFutureTask<>(call, this::inc, Long.MIN_VALUE);
        if (future.compareAndSet(prev, timeAwareFutureTask)) { // successfully exchanged with new future
            executor.execute(timeAwareFutureTask);
            return timeAwareFutureTask;
        } else { // someone executed before us
            return future.get();
        }
    }

    /**
     * Minimal waiting until execute methods updates {@code TimeAwareFutureTask}
     * so that we get newest object
     *
     * @param when
     * @return
     */
    private TimeAwareFutureTask waitUntilAvailable(long when) {
        while (true) { // need refresh 
            TimeAwareFutureTask<T> local = future.get();
            if (local.startAt() > when) {
                return local;
            }
            LockSupport.parkNanos(1);
        }
    }

    private TimeAwareFutureTask<T> resolveGet(long when, boolean update) {
        TimeAwareFutureTask<T> task = future.get();
        if (task.startAt() > when || task.finishedAt() > when) { // called after we, so just get
            return this.waitUntilAvailable(when);
        }

        //else
        if (update) {
            execute();
            return waitUntilAvailable(when);
        } else {
            return future.get();
        }

    }

    /**
     * Get updated value with indefinite amount of wait time
     *
     * @return
     * @throws InterruptedException
     * @throws ExecutionException
     */
    public T getUpdate() throws InterruptedException, ExecutionException {
        return resolveGet(inc(), true).get();
    }

    /**
     * Get updated value with limited amount of wait time
     *
     * @param timeout
     * @param unit
     * @return
     * @throws InterruptedException
     * @throws ExecutionException
     * @throws TimeoutException
     */
    public T getUpdate(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        return resolveGet(inc(), true).get(timeout, unit);
    }

    /**
     * Get last update value. Does not schedule update. If no update was
     * executed before, returns initial value. If update was scheduled, but not
     * finished while this method was called, await to finish.
     *
     * @return
     * @throws InterruptedException
     * @throws ExecutionException
     */
    public T get() throws InterruptedException, ExecutionException {
        return resolveGet(inc(), false).get();
    }

    /**
     * Get last update value. Does not schedule update. If no update was
     * executed before, returns initial value. If update was scheduled, but not
     * finished while this method was called, await to finish.
     *
     * @param timeout
     * @param unit
     * @return
     * @throws InterruptedException
     * @throws ExecutionException
     * @throws TimeoutException
     */
    public T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        return resolveGet(inc(), false).get(timeout, unit);
    }

    /**
     * Manually update current future value.
     *
     * @param val
     */
    public void set(T val) {

        TimeAwareFutureTask<T> prev = future.get();
        TimeAwareFutureTask<T> timeAwareFutureTask = new TimeAwareFutureTask<>(() -> val, this::inc, Long.MIN_VALUE);
        if (future.compareAndSet(prev, timeAwareFutureTask)) { // successfully exchanged with new future
            timeAwareFutureTask.run();
        }

    }

}
