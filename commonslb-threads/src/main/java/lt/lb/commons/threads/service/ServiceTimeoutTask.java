package lt.lb.commons.threads.service;

import java.util.LinkedList;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicLong;
import lt.lb.commons.Java;
import lt.lb.commons.threads.sync.TimeAwareFutureTask;
import lt.lb.commons.threads.sync.WaitTime;

/**
 *
 * @author laim0nas100
 */
public class ServiceTimeoutTask<V> {

    protected final LinkedList<Runnable> onUpdate = new LinkedList<>();
    protected final WaitTime time;
    protected final AtomicLong timedRequests = new AtomicLong(0);
    protected final Callable<V> call;
    protected final AtomicLong lastCompleted = new AtomicLong(Long.MIN_VALUE);
    protected ScheduledExecutorService service;
    protected Executor exe;
    protected AtomicLong commitNr = new AtomicLong(Long.MIN_VALUE);

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
    }
    
    
    
    protected Runnable makeDecrementer(){
        final long currentCommit = commitNr.get();
        return ()->{
             if (commitNr.get() <= currentCommit && onTimeoutArrive()) {
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
        service.schedule(makeDecrementer(), time.time, time.unit);
    }

    public void addOnUpdate(Runnable run) {
        this.onUpdate.add(run);
    }

    protected boolean onTimeoutArrive() {

        long get = timedRequests.decrementAndGet();
        return get == 0;
    }

    protected void cleanup() {

    }

    protected TimeAwareFutureTask<V> executeNow() {
        TimeAwareFutureTask<V> task = new TimeAwareFutureTask<>(call, Java::getNanoTime, Long.MIN_VALUE);
        exe.execute(task);
        return task;
    }
}
