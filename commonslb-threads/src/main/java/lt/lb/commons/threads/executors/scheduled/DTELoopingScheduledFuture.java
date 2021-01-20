package lt.lb.commons.threads.executors.scheduled;

import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import lt.lb.commons.threads.sync.WaitTime;

/**
 *
 * {@inheritDoc}
 *
 * Run method is responsible for rescheduling this future, if it's not been
 * cancelled.
 *
 * @author laim0nas100
 */
public class DTELoopingScheduledFuture<T> extends DTEScheduledFuture<T> {

    public DTELoopingScheduledFuture(DelayedTaskExecutor exe, WaitTime wait, Callable<T> call) {
        super(exe, wait, call);
        this.oneShot = false;
    }

    @Override
    public void run() {
        FutureTask<T> task = taskRef.get();
        task.run();
        if (!task.isDone()) {
            return; // repeated run
        }
        if (task.isCancelled()) {
            return;
        }
        FutureTask<T> futureTask = new FutureTask<>(call);
        if (taskRef.compareAndSet(task, futureTask)) {
            exe.schedule(this);
        }
    }

}
