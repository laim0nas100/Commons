package lt.lb.commons.threads.executors;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import lt.lb.commons.Java;
import lt.lb.commons.threads.sync.WaitTime;
import lt.lb.uncheckedutils.Checked;

/**
 *
 * @author laim0nas100
 */
public class TaskBatcher implements Executor {

    private ArrayDeque<Future> deque = new ArrayDeque<>();
    private BurstExecutor exe;
    private volatile FutureTask<BatchRunSummary> waitingTask = new FutureTask<>(() -> BatchRunSummary.empty());
    private AtomicBoolean inTask = new AtomicBoolean(false);

    public TaskBatcher() {
        this(Java.getAvailableProcessors());
    }

    public TaskBatcher(int paralelism) {
        this.exe = new BurstExecutor(paralelism);
        waitingTask.run();
    }

    public <T> Future<T> execute(Callable<T> call) {
        FutureTask<T> task = new FutureTask<>(call);
        execute(task);
        return task;
    }

    @Override
    public void execute(Runnable command) {
        deque.addFirst(exe.submit(command));
    }

    public static class BatchRunSummary {

        public final int total;
        public final int timedOut;
        public final int interrupted;
        public final int successful;
        public final Collection<Throwable> failures;

        public BatchRunSummary(int total, int ok, int timedOut, int interrupted, Collection<Throwable> th) {
            this.total = total;
            this.successful = ok;
            this.timedOut = timedOut;
            this.interrupted = interrupted;
            this.failures = th;
        }

        public static BatchRunSummary empty() {
            return new BatchRunSummary(0, 0, 0, 0, new ArrayList<>(0));
        }
    }

    /**
     * Await. Stop on first failure.
     *
     * @return
     */
    public BatchRunSummary awaitFailOnFirst() {
        return Checked.checkedCallNoExceptions(() -> {
            return await(true, WaitTime.ofDays(0));// should not throw InterruptedException
        });
    }

    /**
     * Await. Don't stop on failure.
     *
     * @return
     */
    public BatchRunSummary awaitTolerateFails() {
        return Checked.checkedCallNoExceptions(() -> {
            return await(false, WaitTime.ofDays(0));// should not throw InterruptedException
        });
    }

    /**
     * Manual await configuration. Should only call this on 1 waiting thread.
     *
     * @param failFast cancel execution on first failure
     * @param pollWait how long to wait for new task to arrive. Set time to 0 or
     * less to disable;
     * @param executionWait how long to wait for current task to end. Set time
     * to 0 to disable.
     * @return
     */
    public BatchRunSummary await(boolean failFast, WaitTime executionWait) {
        if (this.inTask.compareAndSet(false, true)) {
            ArrayDeque<Future> localDeque = deque;
            deque = new ArrayDeque<>();
            exe.burst();
            Callable<BatchRunSummary> call = () -> {

                int total = 0;
                int ok = 0;
                int interrupted = 0;
                int timeout = 0;
                ArrayList<Throwable> failures = new ArrayList<>();
                boolean waitGet = executionWait.time > 0;
                while (!localDeque.isEmpty()) {
                    Future last = localDeque.pollLast();

                    Throwable err = null;
                    if (last != null) {
                        total++;
                        try {
                            if (waitGet) {
                                last.get(executionWait.time, executionWait.unit);
                            } else {
                                last.get();
                            }
                            ok++;
                        } catch (InterruptedException th) {
                            interrupted++;
                            err = th;
                        } catch (TimeoutException th) {
                            timeout++;
                            err = th;
                        } catch (Throwable th) {
                            err = th;
                        }
                    }

                    if (err != null) {
                        if (failures.size() < 10000) { // something went terribly wrong, don't collect everything.
                            failures.add(err);
                        }

                        if (failFast) {
                            exe.cancelAll(true);
                            break;
                        }
                    }

                }
                if (!inTask.compareAndSet(true, false)) {
                    throw new IllegalStateException("Should not happen");
                }
                return new BatchRunSummary(total, ok, timeout, interrupted, failures);
            };
            this.waitingTask = new FutureTask<>(call);

        }
        return Checked.uncheckedCall(() -> {
            waitingTask.run();
            return waitingTask.get();
        });
    }

}
