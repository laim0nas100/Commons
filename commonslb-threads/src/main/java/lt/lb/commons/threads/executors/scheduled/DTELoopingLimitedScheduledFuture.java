package lt.lb.commons.threads.executors.scheduled;

import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import lt.lb.commons.threads.sync.WaitTime;

/**
 *
 * {@inheritDoc} Also the scheduling looping can be customized with given
 * {@link ScheduleLoopCondition}.
 *
 * @author laim0nas100
 */
public class DTELoopingLimitedScheduledFuture<T> extends DTELoopingScheduledFuture<T> {

    protected ScheduleLoopCondition loopCondition;

    public DTELoopingLimitedScheduledFuture(ScheduleLoopCondition loopCondition, DelayedTaskExecutor exe, WaitTime wait, Callable<T> call) {
        super(exe, wait, call);
        this.loopCondition = Objects.requireNonNull(loopCondition);
    }

    @Override
    public void run() {
        FutureTask<T> task = taskRef.get();
        task.run();

        if (!task.isDone()) {
            return; // repeated run
        }
        if (task.isCancelled()) {
            loopCondition.loopCanceled(task);
            return;
        }
        // not canceled, try schedule again
        FutureTask<T> futureTask = new FutureTask<>(call);
        if (loopCondition.checkIfShouldLoop(task, futureTask)) {
            if (taskRef.compareAndSet(task, futureTask)) {
                exe.schedule(this);
                loopCondition.newScheduleCommitedAfterCheck(task, futureTask);
            } else {
                loopCondition.newScheduleFailedAfterCheck(task, futureTask);
            }
        }
    }

}
