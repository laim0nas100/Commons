package lt.lb.commons.threads.service;

import java.util.Iterator;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
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
    protected final AtomicLong requests = new AtomicLong(0);
    protected final long maxRequestsBeforeExecute;
    protected ConcurrentLinkedDeque<TimeAwareFutureTask<T>> futures = new ConcurrentLinkedDeque<>();
    protected final long timeoutNanos;

    public ServiceRequestCommiter(ScheduledExecutorService service, WaitTime time, WaitTime timeout, long maxRequestsBeforeExecute, Callable<T> run, long requestThreshold, Executor exe) {
        super(service, time, run, exe);
        this.requestThreshold = requestThreshold;
        lastCommitTask.get().run();
        this.maxRequestsBeforeExecute = maxRequestsBeforeExecute;
        this.timeoutNanos = timeout.toDuration().toNanos();
        service.scheduleWithFixedDelay(this::cancelStuck, timeout.time, timeout.time, timeout.unit);
    }

    protected void cancelStuck() {
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

    @Override
    protected void cleanup() {
        long val = timedRequests.get();
        if (val < 0) {
            timedRequests.compareAndSet(val, 0);
        }
        long req = requests.get();
        if (req < 0 || req >= maxRequestsBeforeExecute + 5) {
            requests.compareAndSet(req, 0);
        }
    }

    protected boolean isCancellable(TimeAwareFutureTask<T> task, long lastDoneStarted, long now) {
        if (task.isDone() || task.isCancelled()) {
            return false;
        }
        long n = task.startAt();
        boolean started = n > Long.MIN_VALUE;
        return started && (n < lastDoneStarted || timeoutNanos <= (now - n));
    }

    public void addRequest() {
        update();
        long req = requests.getAndIncrement();
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
        commitNr.incrementAndGet();
        service.execute(this::cleanup);
        long now = Java.getNanoTime();
        timedRequests.set(0);
        requests.set(0);
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
