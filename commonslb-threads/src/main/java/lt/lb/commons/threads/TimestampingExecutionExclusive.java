package lt.lb.commons.threads;

import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicLong;
import lt.lb.commons.F;
import lt.lb.commons.Java;
import lt.lb.commons.threads.sync.WaitTime;
import lt.lb.uncheckedutils.NestedException;

/**
 *
 * @author laim0nas100
 */
public class TimestampingExecutionExclusive<T> extends TimestampingExecution<T> {

    public static class TimestampedFutureEx<T> extends TimestampedFuture<T> {

        protected AtomicLong started = new AtomicLong(Long.MIN_VALUE);
        protected Semaphore executionLock;

        public TimestampedFutureEx(long time, Callable<T> callable, Semaphore executionLock) {
            super(time, callable);
            this.executionLock = executionLock;
        }

        @Override
        public void run() {
            if (started.compareAndSet(Long.MIN_VALUE, Java.getNanoTime())) {
                super.run();
            }
        }

        @Override
        protected void done() {
            executionLock.release();
            super.done();
        }
    }

    protected Semaphore executionLock;

    public TimestampingExecutionExclusive(Executor executor, WaitTime tolerance) {
        this(executor, tolerance, 128);
    }

    public TimestampingExecutionExclusive(Executor executor, WaitTime tolerance, int cycle) {
        super(executor, tolerance, cycle);
        this.executionLock = new Semaphore(1);
    }

    /**
     *
     * @param auto whether to ignore tolerance window for execution
     * @param task
     * @param tolerance tolerance window within execution
     * @return
     */
    @Override
    public TimestampedFuture<T> execute(boolean auto, Callable<T> task, WaitTime tolerance) {

        final long firstNow = Java.getNanoTime();
        lock.lock();

        TimestampedFutureEx<T> last = F.cast(reference.getLastAdded());
        if (last == null) {
            executionLock.acquireUninterruptibly();
            return cyclicAdd(firstNow, task);
        }// last not null

        // first check
        if (!auto && (last.created + tolerance.toNanos() >= firstNow)) {
            lock.unlock();
            return last;
        }

        //first try
        if (executionLock.tryAcquire()) {
            return cyclicAdd(firstNow, task);
        }
        lock.unlock();
        //waiting for execution to end
        executionLock.acquireUninterruptibly();

        lock.lock();

        TimestampedFutureEx<T> newLast = F.cast(reference.getLastAdded());
        if (!auto && (newLast.started.get() + tolerance.toNanos() >= firstNow)) {
            lock.unlock();
            return last;
        } else {//auto or too stale
            if (auto) {// maybe new one fits

                if (newLast.created <= firstNow && newLast.started.get() >= firstNow) {// created before, but executed during waiting
                    lock.unlock();
                    return newLast;
                }
            }
            return cyclicAdd(Java.getNanoTime(), task);
        }

    }

    @Override
    protected TimestampedFuture<T> cyclicAdd(long now, Callable<T> task) {
        //assume we have the lock
        TimestampedFutureEx<T> future = null;
        try {
            future = new TimestampedFutureEx<>(now, task, executionLock);
            executor.execute(future);
            reference.add(future);
            return future;
        } catch (Throwable failedToSubmit) {
            if (future != null) {
                future.setException(failedToSubmit);
                return future;
            } else {
                executionLock.release();
                throw NestedException.of(failedToSubmit);
            }

        } finally {
            lock.unlock();
        }
    }

    @Override
    public TimestampedFuture<T> execute(boolean auto, Callable<T> task) {
        return execute(auto, task, toleranceNanos);
    }

}
