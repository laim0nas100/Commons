package lt.lb.commons.threads.sync;

import java.util.Iterator;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicReference;
import lt.lb.commons.Java;

/**
 *
 * @author laim0nas100
 */
public class ServiceRequestCommiter<T> extends ServiceTimeoutTask<T> {

    protected long requestThreshold;
    protected final WaitTime timeBeforeCancel;
    protected AtomicReference<TimeAwareFutureTask<T>> lastCommitTask = new AtomicReference<>(new TimeAwareFutureTask<>());
    protected ConcurrentLinkedDeque<TimeAwareFutureTask<T>> futures = new ConcurrentLinkedDeque<>();

    /**
     *
     * @param service service to manage calls
     * @param timeToWait how long to wait
     * @param maxRequestsBeforeExecute max possible requests in total before
     * committing an execute
     * @param call Task to execute after timer reaches zero
     * @param exe executor that executes tasks
     */
    public ServiceRequestCommiter(ScheduledExecutorService service,
            WaitTime timeToWait, WaitTime timeBeforeCancel, long maxRequestsBeforeExecute,
            Callable<T> run, long requestThreshold, Executor exe) {
        super(service, timeToWait, maxRequestsBeforeExecute, run, exe);
        this.requestThreshold = requestThreshold;
        lastCommitTask.get().run();
        this.timeBeforeCancel = timeBeforeCancel;
        service.schedule(this::cleanup, timeBeforeCancel.time, timeBeforeCancel.unit);
    }

    @Override
    protected void cleanup() {
        if (timedRequests.get() < 0) {
            timedRequests.set(0);
        }
        if (requests.get() > maxRequestsBeforeExecute) {
            requests.set(0);
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

    protected boolean isCancellable(TimeAwareFutureTask<T> task, long lastDoneStarted, long now) {
        if (task.isDone() || task.isCancelled()) {
            return false;
        }
        long n = task.startAt();
        return (n > Long.MIN_VALUE && n < lastDoneStarted) // this task was started earlier, but allready finished something else that was started later
                || timeBeforeCancel.toDuration().minusNanos(now - n).isNegative();
    }

    public void addRequest() {
        update();
        if (timedRequests.get() >= requestThreshold || requests.get() >= maxRequestsBeforeExecute) {
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

        service.execute(this::cleanup);
        if (timedRequests.get() < 0) {
            timedRequests.set(0);
        }
        if (requests.get() < 0) {
            requests.set(0);
        }
        long now = Java.getNanoTime();
        timedRequests.set(0);
        TimeAwareFutureTask<T> lastTask = lastCommitTask.get();

        if (lastTask.isDone() && lastTask.finishedAt() > now) {
            return lastTask;

        }
        if (lastTask.startAt() > Long.MIN_VALUE && lastTask.startAt() > now) {
            return lastTask;
        }
        TimeAwareFutureTask<T> task = new TimeAwareFutureTask<>(call, Java::getNanoTime, Long.MIN_VALUE);
        if (lastCommitTask.compareAndSet(lastTask, task)) {
            lastCommitTask.set(task);

            exe.execute(task);
            futures.add(task);
            return task;
        }

        return lastCommitTask.get();
    }

    @Override
    protected TimeAwareFutureTask executeNow() {
        TimeAwareFutureTask executeNow = super.executeNow();
        futures.add(executeNow);
        return executeNow;
    }

}
