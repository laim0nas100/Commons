package lt.lb.commons.threads.sync;

import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicLong;

/**
 *
 * @author laim0nas100 Time aware FutureTask using System.nanoTime for time
 */
public class TimeAwareFutureTask<T> extends FutureTask<T> {
    
    protected AtomicLong startAt = new AtomicLong(Long.MIN_VALUE);
    protected AtomicLong finishedAt = new AtomicLong(Long.MIN_VALUE);
    
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
        startAt.set(System.nanoTime());
        super.run();
        finishedAt.set(System.nanoTime());
    }
    
    @Override
    protected boolean runAndReset() {
        startAt.set(System.nanoTime());
        boolean r = super.runAndReset();
        finishedAt.set(System.nanoTime());
        return r;
    }
    
    public long startAtNanos() {
        return startAt.get();
    }
    
    public long finishedAtNanos() {
        return finishedAt.get();
    }
    
}
