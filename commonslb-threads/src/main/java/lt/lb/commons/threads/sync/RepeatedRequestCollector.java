package lt.lb.commons.threads.sync;

import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import lt.lb.commons.F;
import lt.lb.commons.Java;

/**
 *
 * Submit requests to be completed in supplied executor. Instantly begins
 * executing request after submission, if currently running request amount does
 * not exceed parallelRequests parameter.
 *
 * @author laim0nas100
 */
public class RepeatedRequestCollector {

    protected final AtomicInteger pendingRequests = new AtomicInteger(0);
    protected final AtomicInteger requestsRunning = new AtomicInteger(0);
    protected int parallelRequests;

    protected AtomicReference<TimeAwareFutureTask> lastCommitTask = new AtomicReference<>(new TimeAwareFutureTask());

    protected Runnable task;
    protected Executor commitExecutor;

    public RepeatedRequestCollector(int parallelRequests, Runnable run, Executor commitExecutor) {
        this.parallelRequests = parallelRequests;
        this.task = () -> {
            F.checkedRun(run);
            requestsRunning.decrementAndGet();
            if (pendingRequests.getAndSet(0) > 0) {
                executeRequest();
            }
        };
        this.commitExecutor = commitExecutor;
    }

    /**
     * Add request and execute it if it does not exceed already running requests
     * threshold.
     *
     * @return last started future
     */
    public Future addRequest() {
        if (requestsRunning.incrementAndGet() <= parallelRequests) {
            return executeRequest();
        } else {
            requestsRunning.decrementAndGet();
            pendingRequests.incrementAndGet();
            return lastCommitTask.get();
        }
    }

    /**
     * Add request and execute it if it does not exceed already running requests
     * threshold. Makes sure, that latest future was started after this method
     * was called
     *
     * @return last started future,
     */
    public Future addRequestNewest() {
        if (requestsRunning.incrementAndGet() < parallelRequests) {
            return executeRequest();
        } else {
            long nanoTime = Java.getNanoTime();
            requestsRunning.decrementAndGet();
            pendingRequests.incrementAndGet();
            TimeAwareFutureTask lastFuture = lastCommitTask.get();
            if (lastFuture.startAt.get() > nanoTime) {//started after we asked
                return lastFuture;
            } else {
                F.checkedRun(() -> lastFuture.get()); // await 
                return addRequest();
            }
        }
    }

    private TimeAwareFutureTask executeRequest() {
        TimeAwareFutureTask timeAwareFutureTask = new TimeAwareFutureTask(task);
        lastCommitTask.set(timeAwareFutureTask);
        try {
            commitExecutor.execute(timeAwareFutureTask);
        } catch(Throwable ignore) {

        }

        return timeAwareFutureTask;
    }

}
