package lt.lb.commons.threads.sync;

import com.github.laim0nas100.uncheckedutils.Checked;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.LockSupport;
import java.util.function.Supplier;
import lt.lb.commons.Java;

/**
 *
 * Designed to be used with virtual threads, because it spawns many very
 * short-lived threads.
 *
 * @author laim0nas100
 */
public class TimeoutTask {

    // nanos
    protected final long timeout;
    protected final long refreshRate;

    protected final Executor service;
    protected volatile long lastSubmittedStamp = Long.MIN_VALUE;
    protected volatile long lastExecutedStamp = Long.MIN_VALUE;
    protected AtomicLong stamper = new AtomicLong(Long.MIN_VALUE + 1);

    public Supplier<Boolean> conditionalCheck = () -> true;
    protected ArrayList<Runnable> onUpdate = new ArrayList<>();
    protected final Runnable run;

    /**
     *
     * @param timeout wait until execution
     * @param refreshRate Timer update rate after timeout was reached, calls
     * conditional check every time until it succeeds or a new update has been
     * called;
     * @param run Task to execute after timer reaches zero
     */
    public TimeoutTask(WaitTime timeout, WaitTime refreshRate, Runnable run) {
        this(timeout, refreshRate, run, Checked.createDefaultExecutorService());
    }

    /**
     *
     * @param timeout wait until execution
     * @param refreshRate Timer update rate after timeout was reached, calls
     * conditional check every time until it succeeds or a new update has been
     * called;
     * @param run Task to execute after timer reaches zero
     * @param service executor service that handle waiting and execution, very
     * much prefer to use virtual threads.
     */
    public TimeoutTask(WaitTime timeout, WaitTime refreshRate, Runnable run, Executor service) {
        this.timeout = timeout.toNanosAssert();
        this.refreshRate = refreshRate.toNanosAssert();
        this.run = Objects.requireNonNull(run);
        this.service = Objects.requireNonNull(service);
    }

    /**
     * start timer or update timer if already started
     */
    public void update() {
        for (Runnable r : this.onUpdate) {
            r.run();
        }
        long stamp = stamper.getAndIncrement();
        maybeStartNewCall(stamp);

    }

    protected void maybeStartNewCall(final long stamp) {
        long nanoTimeAtStarting = Java.getNanoTime();
        lastSubmittedStamp = stamp;

        service.execute(() -> {
            boolean awaited = false;
            while (lastSubmittedStamp == stamp) {

                long toPark = timeout - (Java.getNanoTime() - nanoTimeAtStarting);
                if (toPark <= 0) {
                    awaited = true;
                    break;
                }
                LockSupport.parkNanos(toPark);

            }
            if (!awaited) {//cancel
                return;
            }
            //awaited
            while (!conditionalCheck.get()) {
                LockSupport.parkNanos(refreshRate);
                if (lastSubmittedStamp != stamp) {
                    return; // cancel;
                }
            }

            lastExecutedStamp = stamp;
            run.run();

        });
    }

    ;

    public void addOnUpdate(Runnable run) {
        this.onUpdate.add(run);
    }
}
