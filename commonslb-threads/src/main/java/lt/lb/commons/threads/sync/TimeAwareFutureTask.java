package lt.lb.commons.threads.sync;

import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicLong;
import lt.lb.commons.Java;

/**
 *
 * @author laim0nas100 Time aware FutureTask using Java.getNanoTime for time
 */
public class TimeAwareFutureTask<T> extends FutureTask<T> {
    
    protected volatile AtomicLong startAt = new AtomicLong(Long.MIN_VALUE);
    protected volatile AtomicLong finishedAt = new AtomicLong(Long.MIN_VALUE);
    
    public TimeAwareFutureTask(Callable<T> callable) {
        super(callable);
    }
    
    public TimeAwareFutureTask(Runnable run) {
        this(Executors.callable(run, null));
    }
    
    public TimeAwareFutureTask() {
        this(() -> null);
    }
    
    @Override
    public void run() {
        startAt.set(Java.getNanoTime());
        super.run();
        finishedAt.set(Java.getNanoTime());
    }
    
    @Override
    protected boolean runAndReset() {
        startAt.set(Java.getNanoTime());
        boolean r = super.runAndReset();
        finishedAt.set(Java.getNanoTime());
        return r;
    }
    
    public long startAtNanos() {
        return startAt.get();
    }
    
    public long finishedAtNanos() {
        return finishedAt.get();
    }
    
}
