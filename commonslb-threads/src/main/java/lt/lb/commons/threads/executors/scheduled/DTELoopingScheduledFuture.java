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
    }

    @Override
    public boolean isOneShot() {
        return false;
    }
    

    @Override
    public void run() {
        if(ref.isCancelled()){
            return;
        }
        FutureTask<T> currentTask = ref.getRef();
        currentTask.run();
        if (!currentTask.isDone()) {
            return; // repeated run, is still running so early exit
        }
        if (ref.isCancelled()) {//the task or scheduling future was cancelled
            return;
        }
        FutureTask<T> futureTask = new FutureTask<>(call);
        if (ref.compareAndSet(currentTask, futureTask)) {
            exe.schedule(this);
        }
    }

}
