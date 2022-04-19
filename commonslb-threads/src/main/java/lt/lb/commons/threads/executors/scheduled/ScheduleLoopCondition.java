package lt.lb.commons.threads.executors.scheduled;

import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

/**
 *
 * @author laim0nas100
 */
public interface ScheduleLoopCondition {

    /**
     * Check if should schedule again
     *
     * @param oldTask
     * @param newTask
     * @return
     */
    public boolean checkIfShouldLoop(FutureTask oldTask, FutureTask newTask);

    /**
     * Calls when scheduled again after successful check for potential clean-up.
     *
     * @param oldTask
     * @param newTask
     */
    public void newScheduleCommitedAfterCheck(FutureTask oldTask, FutureTask newTask);

    /**
     * Calls when failed to scheduled again after successful check for potential
     * clean-up.
     *
     * @param oldTask
     * @param newTask
     */
    public void newScheduleFailedAfterCheck(FutureTask oldTask, FutureTask newTask);

    /**
     * Calls when {@link ScheduledFuture} was cancelled and thus no more
     * scheduling can commence.
     *
     * @param canceledTask
     */
    public void loopCanceled(FutureTask canceledTask);

    public static abstract class AbstractScheduleLoopCondition implements ScheduleLoopCondition {

        @Override
        public void loopCanceled(FutureTask canceledTask) {
        }

        @Override
        public void newScheduleFailedAfterCheck(FutureTask oldTask, FutureTask newTask) {
        }

        @Override
        public void newScheduleCommitedAfterCheck(FutureTask oldTask, FutureTask newTask) {
        }

        @Override
        public abstract boolean checkIfShouldLoop(FutureTask oldTask, FutureTask newTask);

    }

    /**
     * Schedule task given amount of times.
     *
     * @param stopOnFailed if true, task stops scheduling when execution fails
     * @param times
     * @return
     */
    public static ScheduleLoopCondition loopTimes(final boolean stopOnFailed, final int times) {

        return new AbstractScheduleLoopCondition() {
            AtomicInteger counter = new AtomicInteger(times);

            @Override
            public boolean checkIfShouldLoop(FutureTask oldTask, FutureTask newTask) {
                if (stopOnFailed && oldTask.isDone()) {
                    try {
                        oldTask.get();
                    } catch (InterruptedException | ExecutionException ex) {
                        return false;
                    }
                }
                return counter.decrementAndGet() > 0;
            }

            @Override
            public void newScheduleFailedAfterCheck(FutureTask oldTask, FutureTask newTask) {
                counter.incrementAndGet(); // restore failed check
            }
        };
    }

    /**
     * Schedule task while predicate returns true.
     *
     * @param stopOnFailed if true, task stops scheduling when execution fails
     * @param condition
     * @return
     */
    public static ScheduleLoopCondition loopWhen(final boolean stopOnFailed, Predicate<FutureTask<?>> condition) {
        Objects.requireNonNull(condition, "Loop condition is null");
        return new AbstractScheduleLoopCondition() {
            @Override
            public boolean checkIfShouldLoop(FutureTask oldTask, FutureTask newTask) {
                if (stopOnFailed && oldTask.isDone()) {
                    try {
                        oldTask.get();
                    } catch (InterruptedException | ExecutionException ex) {
                        return false;
                    }
                }
                return condition.test(oldTask);
            }
        };
    }
}
