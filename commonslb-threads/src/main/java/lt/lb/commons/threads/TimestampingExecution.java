package lt.lb.commons.threads;

import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import lt.lb.commons.Java;
import lt.lb.commons.Nulls;
import lt.lb.commons.containers.CyclicBuffer;
import lt.lb.commons.threads.sync.WaitTime;

/**
 *
 * @author laim0nas100
 */
public class TimestampingExecution<T> {

    public static class TimestampedFuture<T> extends ExplicitFutureTask<T> {

        public final long created;

        public TimestampedFuture(long time, Callable<T> callable) {
            super(callable);
            this.created = time;
        }

    }

    protected Executor executor;
    protected CyclicBuffer<TimestampedFuture<T>> buffer;

    protected WaitTime toleranceNanos;
    protected ReentrantReadWriteLock lock = new ReentrantReadWriteLock(false);

    public TimestampingExecution(Executor executor, WaitTime tolerance) {
        this(executor, tolerance, 128);
    }

    /**
     * 
     * @param executor
     * @param tolerance
     * @param cycle CyclicBuffer size. If unfinished tasks loop around, deadlock happens. Don't pick too small
     */
    public TimestampingExecution(Executor executor, WaitTime tolerance, int cycle) {
        this.executor = Nulls.requireNonNull(executor);
        this.toleranceNanos = WaitTime.ofNanos(Nulls.requireNonNull(tolerance).toNanosAssert());
        this.buffer = new CyclicBuffer<>(cycle);
    }

    protected TimestampedFuture<T> cyclicAdd(long now, Callable<T> task) {
        //assume we have the lock
        TimestampedFuture<T> future = new TimestampedFuture<>(now, task);
        executor.execute(future);
        buffer.add(future);
        return future;
    }

    /**
     *
     * @param auto whether to ignore tolerance window for execution
     * @param task
     * @param tolerance tolerance window within execution
     * @return
     */
    public TimestampedFuture<T> execute(boolean auto, Callable<T> task, WaitTime tolerance) {

        final long now = Java.getNanoTime();
        try {
            lock.readLock().lock();
            TimestampedFuture<T> last = buffer.getLastAdded();
            if (!auto && last != null && last.created + tolerance.toNanos() >= now) { // within tolerance
                return last;
            } else {
                try {
                    lock.writeLock().lock();
                    return cyclicAdd(now, task);
                } finally {
                    lock.writeLock().unlock();
                }
            }

        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     *
     * @param auto whether to ignore tolerance window for execution
     * @param task
     * @return
     */
    public TimestampedFuture<T> execute(boolean auto, Callable<T> task) {
        return execute(auto, task, toleranceNanos);
    }
}
