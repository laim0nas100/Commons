package lt.lb.commons.threads.sync;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicReference;
import lt.lb.commons.Timer;

/**
 *
 * @author laim0nas100
 */
public class ServiceRequestCommiter<T> extends ServiceTimeoutTask {

    protected long requestThreshold;
    protected AtomicReference<TimeAwareFutureTask<T>> lastCommitTask = new AtomicReference<>(new TimeAwareFutureTask<>());

    public ServiceRequestCommiter(ScheduledExecutorService service, WaitTime time, Callable<T> run, long requestThreshold, Executor exe) {
        super(service, time, run, exe);
        this.requestThreshold = requestThreshold;
    }

    @Override
    protected void cleanup() {
        if (requests.get() < 0) {
            requests.set(0);
        }
    }

    public void addRequest() {
        update();
        if (requests.get() >= requestThreshold) {
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

        if (requests.get() <= 0) {
            requests.set(0);
            return lastCommitTask.get();
        }
        long now = Timer.getNanoTime();
        requests.set(0);
        TimeAwareFutureTask<T> lastTask = lastCommitTask.get();

        if (lastTask.isDone() && lastTask.finishedAtNanos() > now) {
            return lastTask;

        }
        if (lastTask.startAtNanos() > Long.MIN_VALUE && lastTask.startAtNanos() > now) {
            return lastTask;
        }
        TimeAwareFutureTask executeNow = executeNow();
        lastCommitTask.set(executeNow);
        return lastCommitTask.get();
    }
}
