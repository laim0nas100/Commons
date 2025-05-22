package lt.lb.commons.threads.executors.scheduled;

import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.Delayed;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;
import lt.lb.commons.F;
import lt.lb.commons.Java;
import lt.lb.commons.misc.numbers.Atomic;
import lt.lb.commons.threads.sync.WaitTime;

/**
 *
 * {@link ScheduledFuture} base implementation. By design can't run more than
 * one at the time, and only can start to do another task, once this finishes.
 *
 * @author laim0nas100
 * @param <T>
 */
public class DTEScheduledFuture<T> implements ScheduledFuture<T>, RunnableFuture<T> {

    public final AtomicLong nanoScheduled = new AtomicLong(Long.MIN_VALUE);
    public final Callable<T> call;
    public final WaitTime wait;
    public final long waitDurNanos;
    public final Executor taskExecutor;
    protected final DelayedTaskExecutor exe;
    protected final PersistentCancel<T, FutureTask<T>> ref;

    public DTEScheduledFuture(DelayedTaskExecutor exe, WaitTime wait, Callable<T> call) {
        this(Objects.requireNonNull(exe), exe.realExe, wait, call);
    }

    public DTEScheduledFuture(DelayedTaskExecutor exe, Executor taskExecutor, WaitTime wait, Callable<T> call) {
        this.exe = Objects.requireNonNull(exe);
        this.taskExecutor = taskExecutor;
        this.call = Objects.requireNonNull(call);
        this.ref = new PersistentCancel<>(new FutureTask<>(call));
        this.wait = Objects.requireNonNull(wait);
        waitDurNanos = wait.convert(TimeUnit.NANOSECONDS).time;
    }

    public boolean isOneShot() {
        return true;
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

    protected void logic() {
        ref.getRef().run();
    }

    @Override
    public final void run() {
        try {
            logic();
        } finally {
            Atomic.decrementAndGet(exe.executing);
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
