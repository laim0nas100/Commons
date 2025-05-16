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

    protected void logic() {
        FutureTask<T> currentTask = ref.getRef();
        if (ref.isCancelled()) {
            loopCondition.loopCanceled(currentTask);
            return;
        }
        currentTask.run();
        if (!currentTask.isDone()) {
            return; // repeated run, is still running so early exit
        }

        if (ref.isCancelled()) {
            loopCondition.loopCanceled(currentTask);
            return;
        }
        // not canceled, try schedule again
        FutureTask<T> futureTask = new FutureTask<>(call);
        if (loopCondition.checkIfShouldLoop(currentTask, futureTask)) {
            if (ref.compareAndSet(currentTask, futureTask)) {
                exe.schedule(this);
                loopCondition.newScheduleCommitedAfterCheck(currentTask, futureTask);
            } else {
                loopCondition.newScheduleFailedAfterCheck(currentTask, futureTask);
            }
        }
    }

}
