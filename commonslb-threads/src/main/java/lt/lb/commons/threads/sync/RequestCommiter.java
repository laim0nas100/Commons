package lt.lb.commons.threads.sync;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import lt.lb.commons.F;
import lt.lb.commons.JavaProperties;
import lt.lb.commons.Timer;

/**
 * Use ServiceRequestCommiter
 * @author laim0nas100
 */
@Deprecated
public class RequestCommiter<T> {

    protected final AtomicInteger pendingRequests = new AtomicInteger(0);
    protected final AtomicBoolean timeoutRunning = new AtomicBoolean(false);
    protected long requestThreshhold;
    protected long timeoutSeconds;

    protected Callable<T> task;
    protected volatile FutureTask<T> lastCommitTask;
    protected volatile long lastRequestAdd = JavaProperties.getNanoTime();
    protected Executor commitExecutor;

    protected Runnable timeRun = () -> {
        F.checkedRun(() -> {
            long sleepTime = 0;
            do {
                if (sleepTime > 0) {
                    Thread.sleep(sleepTime);
                }
                long lastRq = this.getLastRequestAdd();
                
                long now = JavaProperties.getNanoTime();
                
                sleepTime = (now - lastRq)/10000000; // to milliseonds
                if (sleepTime <= 0) {
                    break;
                }
            } while (true);
            commit();
        });
        timeoutRunning.set(false);
    };

    public RequestCommiter(long timeoutSeconds, long requestThreshold, Callable<T> call, Executor commitExecutor) {
        this.requestThreshhold = requestThreshold;
        this.task = call;
        this.timeoutSeconds = timeoutSeconds;
        this.commitExecutor = commitExecutor;
    }

    public void addRequest() {
        
        lastRequestAdd = JavaProperties.getNanoTime();
        update();
        if (pendingRequests.incrementAndGet() >= requestThreshhold) {
            commit();
        }

    }

    protected void update() {

        if (timeoutRunning.compareAndSet(false, true)) {
            Thread thread = new Thread(timeRun);
            thread.start();
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
        if (pendingRequests.get() == 0) {
            return lastCommitTask;
        }
        pendingRequests.set(0);
        FutureTask<T> commit = new FutureTask<>(task);
        lastCommitTask = commit;
        this.commitExecutor.execute(commit);
        return commit;
    }

    protected long getLastRequestAdd() {
        return this.lastRequestAdd;
    }

}
