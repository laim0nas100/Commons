package lt.lb.commons.threads.executors;

import java.util.Objects;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import lt.lb.commons.threads.executors.scheduled.DelayedTaskExecutor;
import lt.lb.commons.threads.sync.WaitTime;

/**
 *
 * @author laim0nas100
 */
public class DelayedWaveExecutor implements Executor {

    protected Executor executor;
    protected ScheduledExecutorService service;
    protected WaitTime wt;

    protected ConcurrentLinkedDeque<Runnable> deque = new ConcurrentLinkedDeque<>();
    protected AtomicInteger pending = new AtomicInteger(0);
    protected AtomicBoolean inScheduled = new AtomicBoolean(false);

    protected boolean inPlace = false;

    protected Runnable r = () -> {
        if (inScheduled.get()) {
            while (true) {
                long andSet = pending.getAndSet(0);
                for (int i = 0; i < andSet; i++) {
                    executor.execute(deque.pollFirst());
                }
                if (andSet > 0) {
                    if (!inPlace) {
                        schedule();
                        return;
                    }
                } else {
                    inScheduled.set(false);
                    return;
                }
            }

        }
    };

    public DelayedWaveExecutor(Executor executor, ScheduledExecutorService service, WaitTime wt) {
        this.executor = Objects.requireNonNull(executor);
        this.service = Objects.requireNonNull(service);
        this.wt = wt;
        this.inPlace = false;
    }

    public DelayedWaveExecutor(Executor executor) {
        this(executor, new DelayedTaskExecutor(1, new InPlaceExecutor()), WaitTime.ofMillis(1));
        inPlace = true;
    }

    protected void schedule() {
        service.schedule(r, wt.time, wt.unit);
    }

    @Override
    public void execute(Runnable command) {
        deque.addLast(command);
        pending.incrementAndGet();
        if (inScheduled.compareAndSet(false, true)) {
            if (inPlace) {
                r.run();
            } else {
                schedule();
            }
        }
    }

}
