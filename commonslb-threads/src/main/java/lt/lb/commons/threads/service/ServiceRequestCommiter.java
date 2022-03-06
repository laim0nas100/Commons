package lt.lb.commons.threads.service;

import java.util.Iterator;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import lt.lb.commons.Java;
import lt.lb.commons.threads.sync.TimeAwareFutureTask;
import lt.lb.commons.threads.sync.WaitTime;

/**
 *
 * @author laim0nas100
 */
public class ServiceRequestCommiter<T> extends ServiceTimeoutTask {

    protected long requestThreshold;
    protected AtomicReference<TimeAwareFutureTask<T>> lastCommitTask = new AtomicReference<>(new TimeAwareFutureTask<>());
    protected final AtomicLong requestsBeforeExecute = new AtomicLong(0);
    protected final long maxRequestsBeforeExecute;
    protected ConcurrentLinkedDeque<TimeAwareFutureTask<T>> futures = new ConcurrentLinkedDeque<>();
    protected final long timeoutNanos;
    
    private static final int MAX_THRESHOLD_ERROR_BAR = 5;
    
    private static final int MAX_FUTURES_BEFORE_CLEANUP = 1024;

    /**
     *
     * @param timeout
     * @param untimedRequestThreashold max threshold of requests that increment
     * and don't decrement via timeout, only reset to zero when commit is made
     * @param timedRequestThreshold max threshold of requests that increment and
     * decrement via timeout
     * @param service service to manage calls
     * @param time how long until a single request timeout
     * @param call Task to execute after timer reaches zero
     * @param exe executor that executes tasks
     */
    public ServiceRequestCommiter(ScheduledExecutorService service, Executor exe, WaitTime time, WaitTime timeout, long untimedRequestThreashold, Callable<T> call, long timedRequestThreshold) {
        super(service, exe, time, call);
        this.requestThreshold = timedRequestThreshold;
        lastCommitTask.get().run();
        this.maxRequestsBeforeExecute = untimedRequestThreashold;
        this.timeoutNanos = timeout.toDuration().toNanos();
    }

    protected void cancelStuck() {
        if (futures.isEmpty()) {
            return;
        }
        Iterator<TimeAwareFutureTask<T>> iterator = futures.iterator();
        long lastDoneStarted = Long.MIN_VALUE;
        long lastDoneFinished = Long.MIN_VALUE;
        while (iterator.hasNext()) {
            TimeAwareFutureTask<T> future = iterator.next();
            if (future == null) {
                continue;
            }
            if (lastDoneFinished < future.finishedAt()) {
                lastDoneStarted = future.startAt();
                lastDoneFinished = future.finishedAt();
            }

        }
        iterator = futures.iterator();
        long now = Java.getNanoTime();
        while (iterator.hasNext()) {
            TimeAwareFutureTask<T> future = iterator.next();
            if (future == null) {
                continue;
            }
            if (isCancellable(future, lastDoneStarted, now)) {
                future.cancel(true);
            }

            if (future.isDone()) {
                iterator.remove();
            }

        }
    }

    protected AtomicBoolean inCleanup = new AtomicBoolean(false);
    @Override
    protected void cleanup() {
        if(!inCleanup.compareAndSet(false, true)){
            return;
        }
        long val = timedRequests.get();
        if (val < 0) {
            timedRequests.compareAndSet(val, 0);
        }
        long req = requestsBeforeExecute.get();
        if (req < 0 || req >= maxRequestsBeforeExecute + MAX_THRESHOLD_ERROR_BAR) {
            requestsBeforeExecute.compareAndSet(req, 0);
        }
        if(futures.size() >= MAX_FUTURES_BEFORE_CLEANUP){
            cancelStuck();
        }
        inCleanup.set(false);
    }

    protected boolean isCancellable(TimeAwareFutureTask<T> task, long lastDoneStarted, long now) {
        if (task.isDone() || task.isCancelled()) {
            return false;
        }
        long n = task.startAt();
        return (n > Long.MIN_VALUE) && (n < lastDoneStarted || timeoutNanos <= (now - n));
    }

    public void addRequest() {
        update();
        long req = requestsBeforeExecute.getAndIncrement();
        long timedRed = timedRequests.get();
        if (timedRed >= requestThreshold || req >= maxRequestsBeforeExecute) {
            commit();
        }
    }

    public void commit(boolean wait) throws InterruptedException, ExecutionException {
        if (wait) {
            commit().get();
        } else {
            commit();
        }
    }

    public Future<T> commit() {

        long now = Java.getNanoTime();

        TimeAwareFutureTask<T> lastTask = lastCommitTask.get();

        if (lastTask.isDone() && lastTask.finishedAt() > now) {
            return lastTask;

        }
        if (lastTask.startAt() > Long.MIN_VALUE && lastTask.startAt() > now) {
            return lastTask;
        }
        TimeAwareFutureTask<T> task = new TimeAwareFutureTask<>(call, Java::getNanoTime, Long.MIN_VALUE);
        if (lastCommitTask.compareAndSet(lastTask, task)) {
            commitNr.incrementAndGet();// actually  start new task
            timedRequests.set(0);
            requestsBeforeExecute.set(0);
            service.execute(this::cleanup);
            lastCommitTask.set(task);

            exe.execute(task);
            futures.add(task);
            return task;
        }

        return lastCommitTask.get();
    }

    @Override
    protected TimeAwareFutureTask<T> executeNow() {
        TimeAwareFutureTask<T> executeNow = super.executeNow();
        futures.add(executeNow);
        return executeNow;
    }

}
