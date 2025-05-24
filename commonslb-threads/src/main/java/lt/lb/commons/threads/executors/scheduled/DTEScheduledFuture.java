package lt.lb.commons.threads.executors.scheduled;

import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.Delayed;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;
import lt.lb.commons.F;
import lt.lb.commons.Java;
import lt.lb.commons.misc.numbers.Atomic;
import lt.lb.commons.threads.ExplicitFutureTask;
import lt.lb.commons.threads.sync.WaitTime;
import lt.lb.commons.threads.FailableRunnableFuture;

/**
 *
 * {@link ScheduledFuture} base implementation. By design can't run more than
 * one at the time, and only can start to do another task, once this finishes.
 *
 * @author laim0nas100
 * @param <T>
 */
public class DTEScheduledFuture<T> implements ScheduledFuture<T>, FailableRunnableFuture<T> {

    public final AtomicLong nanoScheduled = new AtomicLong(Long.MIN_VALUE);
    public final Callable<T> call;
    public final WaitTime wait;
    public final long waitDurNanos;
    public final Executor taskExecutor;
    protected final boolean oneShot;
    protected final DelayedTaskExecutor exe;
    protected final PersistentCancel<T, ExplicitFutureTask<T>> ref;

    public DTEScheduledFuture(DelayedTaskExecutor exe, WaitTime wait, Callable<T> call) {
        this(true, Objects.requireNonNull(exe), exe.realExe, wait, call);
    }

    public DTEScheduledFuture(DelayedTaskExecutor exe, Executor taskExecutor, WaitTime wait, Callable<T> call) {
        this(true, exe, taskExecutor, wait, call);
    }

    public DTEScheduledFuture(boolean oneShot, DelayedTaskExecutor exe, Executor taskExecutor, WaitTime wait, Callable<T> call) {
        this.oneShot = oneShot;
        this.exe = Objects.requireNonNull(exe);
        this.taskExecutor = Objects.requireNonNull(taskExecutor);
        this.call = Objects.requireNonNull(call);
        this.ref = new PersistentCancel<>(new ExplicitFutureTask<>(call));
        this.wait = Objects.requireNonNull(wait);
        waitDurNanos = wait.toNanosAssert();
    }

    public boolean isOneShot() {
        return oneShot;
    }

    @Override
    public long getDelay(TimeUnit unit) {
        return getDelay(unit, Java.getNanoTime());
    }

    public long getDelay(TimeUnit unit, long nowNanos) {
        long timeNanoDiff = nowNanos - nanoScheduled.get();
        return unit.convert(waitDurNanos - timeNanoDiff, TimeUnit.NANOSECONDS);

    }

    @Override
    public int compareTo(Delayed o) {
        if (o == null) {
            return 1;
        }
        if (o instanceof DTEScheduledFuture) {
            DTEScheduledFuture other = F.cast(o);
            return Long.compare(waitDurNanos, other.waitDurNanos);
        } else {
            return Long.compare(this.getDelay(TimeUnit.NANOSECONDS), o.getDelay(TimeUnit.NANOSECONDS));
        }

    }

    @Override
    public boolean isDone() {
        return ref.isDone();
    }

    @Override
    public boolean isCancelled() {
        return ref.isCancelled();
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return ref.cancel(mayInterruptIfRunning);
    }

    @Override
    public void setException(Throwable t) {
        ref.setException(t);
    }

    protected void logic() {
        ref.run();
    }

    @Override
    public final void run() {
        try {
            logic();
        } finally {
            int decrementAndGet = Atomic.decrementAndGet(exe.executing);
        }
    }

    @Override
    public T get() throws InterruptedException, ExecutionException {
        return ref.get();
    }

    @Override
    public T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        return ref.get(timeout, unit);
    }

}
