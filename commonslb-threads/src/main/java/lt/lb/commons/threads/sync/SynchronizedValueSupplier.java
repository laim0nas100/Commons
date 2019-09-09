package lt.lb.commons.threads.sync;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.LockSupport;

/**
 *
 * @author laim0nas100
 */
public class SynchronizedValueSupplier<T> {

    private AtomicLong counter = new AtomicLong(0);
    private Callable<T> call;
    private Executor executor;

    private volatile TimeAwareFutureTask<T> future;

    private volatile long lastExecuteCall = -1;

    public SynchronizedValueSupplier(T initial, Callable<T> call, Executor executor) {
        this.call = call;
        this.executor = executor;
        future = new TimeAwareFutureTask<>(() -> initial, this::inc, Long.MIN_VALUE);
        future.run();
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
        lastExecuteCall = this.inc();
        TimeAwareFutureTask<T> timeAwareFutureTask = new TimeAwareFutureTask<>(call, this::inc, Long.MIN_VALUE);
        future = timeAwareFutureTask;

        executor.execute(future);
        return timeAwareFutureTask;
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
            TimeAwareFutureTask<T> local = future;
            if (local.startAt() > when || lastExecuteCall >= when) {
                return local;
            }
            LockSupport.parkNanos(1);
        }
    }

    private TimeAwareFutureTask<T> resolveGet(long when, boolean update) {
        if (lastExecuteCall > when) { // called after we, so just get
            return this.waitUntilAvailable(when);
        }

        //else
        if (update) {
            execute();
            return waitUntilAvailable(when);
        } else {
            if (lastExecuteCall == -1) { // was never called before, maybe wants the initial value (non-updated)
                return future;
            }
            return this.waitUntilAvailable(lastExecuteCall);
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
     * @param val 
     */
    public void set(T val) {
        lastExecuteCall = inc();
        TimeAwareFutureTask<T> timeAwareFutureTask = new TimeAwareFutureTask<>(() -> val, this::inc, Long.MIN_VALUE);
        future = timeAwareFutureTask;
        timeAwareFutureTask.run();

    }

}
