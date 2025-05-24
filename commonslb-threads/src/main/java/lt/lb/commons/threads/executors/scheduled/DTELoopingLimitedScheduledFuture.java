package lt.lb.commons.threads.executors.scheduled;

import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import lt.lb.commons.threads.ExplicitFutureTask;
import lt.lb.commons.threads.sync.WaitTime;

/**
 *
 * {@inheritDoc} Also the scheduling looping can be customized with given
 * {@link ScheduleLoopCondition}.
 *
 * @author laim0nas100
 */
public class DTELoopingLimitedScheduledFuture<T> extends DTEScheduledFuture<T> {

    protected ScheduleLoopCondition loopCondition;

    public DTELoopingLimitedScheduledFuture(ScheduleLoopCondition loopCondition, DelayedTaskExecutor exe, WaitTime wait, Callable<T> call) {
        this(loopCondition, exe, exe.realExe, wait, call);
    }

    public DTELoopingLimitedScheduledFuture(ScheduleLoopCondition loopCondition, DelayedTaskExecutor exe, Executor taskExecutor, WaitTime wait, Callable<T> call) {
        super(false, exe, taskExecutor, wait, call);
        this.loopCondition = Objects.requireNonNull(loopCondition);
    }

    @Override
    protected void logic() {
        ExplicitFutureTask<T> currentTask = ref.getRef();
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

        if (exe.isShutdown()) {
            return;
        }
        // not canceled, try schedule again
        ExplicitFutureTask<T> futureTask = new ExplicitFutureTask<>(call);
        if (loopCondition.checkIfShouldLoop(currentTask, futureTask)) {
            if (ref.compareAndSet(currentTask, futureTask)) {
                exe.scheduleForSure(this);
                loopCondition.newScheduleCommitedAfterCheck(currentTask, futureTask);
            } else {
                loopCondition.newScheduleFailedAfterCheck(currentTask, futureTask);
            }
        }
    }

}
