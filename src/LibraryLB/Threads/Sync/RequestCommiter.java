/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package LibraryLB.Threads.Sync;

import LibraryLB.Tracer;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 * @author Laimonas-Beniusis-PC
 */
public class RequestCommiter<T> {

    Tracer t = Tracer.get("RequestCommiter");
    protected final AtomicInteger pendingRequests = new AtomicInteger(0);
    protected final AtomicBoolean timeoutRunning = new AtomicBoolean(false);
    protected long requestThreshhold;
    protected long timeoutSeconds;

    protected Callable<T> task;
    protected FutureTask<T> lastCommitTask;
    protected LocalDateTime lastRequestAdd = LocalDateTime.now();
    protected Executor commitExecutor;

    protected Callable timeRun = () -> {

        long sleepTime = 0;
        do {
            if (sleepTime > 0) {
                t.print("Sleeping for:" + sleepTime);
                Thread.sleep(sleepTime);
            }
            LocalDateTime lastRq = this.getLastRequestAdd();
            LocalDateTime now = LocalDateTime.now();
            // get time difference after sleep
            Duration between = java.time.Duration.between(now, lastRq.plusSeconds(timeoutSeconds));

            if (between.isNegative() || between.isZero()) {
                t.print("Break out");
                break;
            }
            sleepTime = between.toMillis();
        } while (true);
        commit();
        timeoutRunning.set(false);
        return null;
    };

    public RequestCommiter(long timeoutSeconds, long requestThreshold, Callable<T> call, Executor commitExecutor) {
        this.requestThreshhold = requestThreshold;
        this.task = call;
        this.timeoutSeconds = timeoutSeconds;
        this.commitExecutor = commitExecutor;
    }

    public void addRequest() {
        lastRequestAdd = LocalDateTime.now();
        update();
        if (pendingRequests.incrementAndGet() >= requestThreshhold) {
            commit();
        }

    }

    protected void update() {

        if (timeoutRunning.compareAndSet(false, true)) {
            Thread thread = new Thread(new FutureTask<>(timeRun));
            thread.setName("Timeout thread:" + thread.getName());
            thread.start();
            t.print("Start timeout thread");
        } else {
            t.print("Don't start timeout thread");
        }

    }

    public void commit(boolean wait) throws InterruptedException, ExecutionException {
        if (wait) {
            commit().get();
        } else {
            commit();
        }
    }

    public FutureTask commit() {
        if (pendingRequests.get() == 0) {
            t.print("No pending requests, return previous commit");
            return lastCommitTask;
        }
        pendingRequests.set(0);
        FutureTask<T> commit = new FutureTask<>(task);
        lastCommitTask = commit;
        this.commitExecutor.execute(commit);
        t.print("Submit commit task");
        return commit;
    }

    protected LocalDateTime getLastRequestAdd() {
        return this.lastRequestAdd;
    }

}
