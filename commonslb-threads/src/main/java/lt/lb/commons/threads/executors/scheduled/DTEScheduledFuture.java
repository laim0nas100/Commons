package lt.lb.commons.threads.executors.scheduled;

import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.Delayed;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import lt.lb.commons.F;
import lt.lb.commons.Java;
import lt.lb.commons.threads.ForwardingFuture;
import lt.lb.commons.threads.sync.WaitTime;

/**
 *
 * {@link ScheduledFuture} base implementation. By design can't run more than
 * one at the time, and only can start to do another task, once this finishes.
 *
 * @author laim0nas100
 * @param <T>
 */
public class DTEScheduledFuture<T> implements ScheduledFuture<T>, ForwardingFuture<T>, RunnableFuture<T> {

    public final AtomicLong nanoScheduled = new AtomicLong();
    protected final AtomicReference<FutureTask<T>> taskRef;
    public final Callable<T> call;
    public final WaitTime wait;
    public final long waitDurNanos;
    protected DelayedTaskExecutor exe;

    public DTEScheduledFuture(DelayedTaskExecutor exe, WaitTime wait, Callable<T> call) {
        this.exe = Objects.requireNonNull(exe);
        this.call = Objects.requireNonNull(call);
        this.taskRef = new AtomicReference<>(new FutureTask<>(call));
        this.wait = Objects.requireNonNull(wait);
        waitDurNanos = wait.toDuration().toNanos();
    }
    
    public boolean isOneShot(){
        return true;
    }

    @Override
    public Future<T> delegate() {
        return taskRef.get();
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
        DTEScheduledFuture other = F.cast(o);
        long now = Java.getNanoTime();
        return Long.compare(getDelay(TimeUnit.NANOSECONDS, now), other.getDelay(TimeUnit.NANOSECONDS, now));
    }

    @Override
    public void run() {
        taskRef.get().run();
    }

}
