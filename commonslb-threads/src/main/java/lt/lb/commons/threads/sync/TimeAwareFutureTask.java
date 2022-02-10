package lt.lb.commons.threads.sync;

import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;
import lt.lb.commons.Java;

/**
 *
 * @author laim0nas100 Time aware FutureTask using Java.getNanoTime or any
 * custom incrementing counter/timer.
 */
public class TimeAwareFutureTask<T> extends FutureTask<T> {

    protected volatile AtomicLong startAt;
    protected volatile AtomicLong finishedAt;
    protected Supplier<Long> timeCheck;

    public TimeAwareFutureTask(Callable<T> callable, Supplier<Long> supp, long initialTimeValue) {
        super(callable);
        timeCheck = Objects.requireNonNull(supp);
        startAt = new AtomicLong(initialTimeValue);
        finishedAt = new AtomicLong(initialTimeValue);
    }

    public TimeAwareFutureTask(Runnable run) {
        this(Executors.callable(run, null), () -> Java.getNanoTime(), Long.MIN_VALUE);
    }

    public TimeAwareFutureTask() {
        this(() -> {
        });
    }

    @Override
    public void run() {
        if (this.isDone()) {
            return;
        }
        startAt.set(timeCheck.get());
        super.run();
        finishedAt.set(timeCheck.get());
    }

    @Override
    protected boolean runAndReset() {
        startAt.set(timeCheck.get());
        boolean r = super.runAndReset();
        finishedAt.set(timeCheck.get());
        return r;
    }

    public long startAt() {
        return startAt.get();
    }

    public long finishedAt() {
        return finishedAt.get();
    }

}
