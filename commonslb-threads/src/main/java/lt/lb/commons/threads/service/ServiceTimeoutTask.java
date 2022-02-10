package lt.lb.commons.threads.service;

import java.util.LinkedList;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicLong;
import lt.lb.commons.Java;
import lt.lb.commons.threads.sync.TimeAwareFutureTask;
import lt.lb.commons.threads.sync.WaitTime;

/**
 *
 * @author laim0nas100
 */
public class ServiceTimeoutTask<T> {

    protected final LinkedList<Runnable> onUpdate = new LinkedList<>();
    protected final WaitTime time;
    protected final AtomicLong timedRequests = new AtomicLong(0);
    protected final Callable<T> call;
    protected final AtomicLong lastCompleted = new AtomicLong(Long.MIN_VALUE);
    protected ScheduledExecutorService service;
    protected Executor exe;
    protected AtomicLong commitNr = new AtomicLong(Long.MIN_VALUE);

    /**
     *
     * @param service service to manage calls
     * @param time how long until a single request timeout
     * @param call Task to execute after timer reaches zero
     * @param exe executor that executes tasks
     */
    public ServiceTimeoutTask(ScheduledExecutorService service, WaitTime time, Callable<T> call, Executor exe) {
        this.service = service;
        this.exe = exe;
        this.time = time;
        this.call = call;
    }

    protected Runnable makeDecrementer() {
        final long currentCommit = commitNr.get();
        return () -> {
            if(!Objects.equals(currentCommit, commitNr.get())){ // manual commit, don't decrement
                return;
            }
            if(!onTimeoutArrive()){// this function decrements timedRequests
                return;
            }
            if (commitNr.compareAndSet(currentCommit, currentCommit + 1)) {// make sure not to override manual commit
                executeNow();
                cleanup();
            }
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
        return timedRequests.decrementAndGet() == 0;
    }

    protected void cleanup() {

    }

    /**
     * Should increment the commitNr when executing
     * @return 
     */
    protected TimeAwareFutureTask<T> executeNow() {
        TimeAwareFutureTask<T> task = new TimeAwareFutureTask<>(call, Java::getNanoTime, Long.MIN_VALUE);
        exe.execute(task);
        return task;
    }
}
