package lt.lb.commons.threads.sync;

import java.util.LinkedList;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicLong;

/**
 *
 * @author laim0nas100
 */
public class ServiceTimeoutTask<V> {

    protected final LinkedList<Runnable> onUpdate = new LinkedList<>();
    protected final WaitTime time;
    protected final AtomicLong requests = new AtomicLong(0);
    protected final Runnable decrementer;
    protected final Callable<V> call;
    protected final AtomicLong lastCompleted = new AtomicLong(Long.MIN_VALUE);

    protected ScheduledExecutorService service;
    protected Executor exe;

    /**
     *
     * @param service service to manage calls
     * @param time how long until a timeout
     * @param call Task to execute after timer reaches zero
     * @param exe executor that executes tasks
     */
    public ServiceTimeoutTask(ScheduledExecutorService service, WaitTime time, Callable<V> call, Executor exe) {
        this.service = service;
        this.exe = exe;
        this.time = time;
        this.call = call;
        decrementer = () -> {
            if (onTimeoutArrive()) {
                executeNow();
            }
            cleanup();

        };
    }

    /**
     * start timer or update timer if already started
     */
    public void update() {
        for (Runnable r : this.onUpdate) {
            r.run();
        }
        requests.incrementAndGet();

        service.schedule(decrementer, time.time, time.unit);
    }

    public void addOnUpdate(Runnable run) {
        this.onUpdate.add(run);
    }

    protected boolean onTimeoutArrive() {
        return requests.decrementAndGet() == 0;
    }

    protected void cleanup() {

    }

    protected TimeAwareFutureTask<V> executeNow() {
        TimeAwareFutureTask<V> task = new TimeAwareFutureTask<>(call);
        exe.execute(task);
        return task;
    }
}
