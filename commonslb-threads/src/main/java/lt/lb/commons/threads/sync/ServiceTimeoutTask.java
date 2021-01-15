package lt.lb.commons.threads.sync;

import java.util.LinkedList;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicLong;
import lt.lb.commons.Java;

/**
 *
 * @author laim0nas100
 */
public class ServiceTimeoutTask<V> {

    protected final LinkedList<Runnable> onUpdate = new LinkedList<>();
    protected final WaitTime timeToWait;
    protected final AtomicLong timedRequests = new AtomicLong(0);
    protected final AtomicLong requests = new AtomicLong(0);
    protected final long maxRequestsBeforeExecute;
    protected final Runnable decrementer;
    protected final Callable<V> call;
    protected final AtomicLong lastCompleted = new AtomicLong(Long.MIN_VALUE);
    protected ScheduledExecutorService service;
    protected Executor exe;

    /**
     *
     * @param service service to manage calls
     * @param timeToWait how long to wait
     * @param maxRequestsBeforeExecute max possible requests in total before
     * commiting an execute
     * @param call Task to execute after timer reaches zero
     * @param exe executor that executes tasks
     */
    public ServiceTimeoutTask(ScheduledExecutorService service, WaitTime timeToWait, long maxRequestsBeforeExecute, Callable<V> call, Executor exe) {
        this.service = service;
        this.exe = exe;
        this.timeToWait = timeToWait;
        this.call = call;
        this.maxRequestsBeforeExecute = maxRequestsBeforeExecute;
        decrementer = () -> {
            if (onTimeoutArrive()) {
                executeNow();
            } else if (maxRequestsBeforeExecute <= requests.get()) {
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
        timedRequests.incrementAndGet();
        requests.incrementAndGet();
        service.schedule(decrementer, timeToWait.time, timeToWait.unit);
    }

    public void addOnUpdate(Runnable run) {
        this.onUpdate.add(run);
    }

    protected boolean onTimeoutArrive() {
        return timedRequests.decrementAndGet() == 0;
    }

    protected void cleanup() {

    }

    protected TimeAwareFutureTask<V> executeNow() {
        TimeAwareFutureTask<V> task = new TimeAwareFutureTask<>(call, Java::getNanoTime, Long.MIN_VALUE);
        exe.execute(task);
        return task;
    }
}
