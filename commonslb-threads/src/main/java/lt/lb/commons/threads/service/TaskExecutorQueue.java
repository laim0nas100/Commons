package lt.lb.commons.threads.service;

import java.io.Serializable;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import lt.lb.commons.threads.Futures;
import lt.lb.commons.threads.service.TaskExecutorQueue.RunInfo;
import lt.lb.uncheckedutils.Checked;
import lt.lb.uncheckedutils.PassableException;
import lt.lb.uncheckedutils.SafeOpt;
import lt.lb.uncheckedutils.func.UncheckedRunnable;

/**
 *
 * @author laim0nas100
 * @param <P>
 * @param <R>
 */
public interface TaskExecutorQueue<P extends Serializable, R extends RunInfo<P>> extends ServiceExecutorAggregator{

    public static interface RunInfo<P extends Serializable> {

        public boolean isUnique();

        public P getKey();

        public default String getName() {
            return String.valueOf(getKey());
        }
    }

    public ScheduledExecutorService getScheduler();

    public ExecutorService getExecutor();

    public default ScheduledFuture<?> schedulePeriodically(R info, long first, long period, TimeUnit unit, UncheckedRunnable run) {
        return getScheduler().scheduleAtFixedRate(() -> {
            submit(info, run);
        }, first, period, unit);
    }

    public default ScheduledFuture<Future<Optional<Throwable>>> schedule(R info, boolean unique, long first, TimeUnit unit, UncheckedRunnable run) {
        return getScheduler().schedule(() -> {
            return submit(info, run);
        }, first, unit);
    }

    public default Future<Optional<Throwable>> submit(R info, UncheckedRunnable run) {
        if (tryEnqueue(info)) {
            return enqueue(info, run);
        } else {
            dequeue(false, info);
        }
        return Futures.done(onFailedEnqueue(info));
    }

    public default Optional<Throwable> submitWait(R info, UncheckedRunnable run) {
        return SafeOpt.ofFuture(submit(info, run)).flatMapOpt(m -> m).asOptional();
    }

    /**
     * Should call dequeue itself after completing the task
     *
     * @param info
     * @param run
     * @return
     */
    public Future<Optional<Throwable>> enqueue(R info, UncheckedRunnable run);

    public boolean tryEnqueue(R info);

    public void dequeue(boolean ran, R info);

    public default Optional<Throwable> onFailedEnqueue(R info) {
        return Optional.of(new PassableException("Failed to enqueue " + info.getName() + " with key:" + info.getKey()));
    }

    public default Optional<Throwable> runUnbounded(R info, UncheckedRunnable run) {

        Optional<Throwable> checkedRun = Checked.checkedRun(beforeRun(info, run));
        afterRun(info, checkedRun);
        return checkedRun;
    }

    public default UncheckedRunnable beforeRun(R info, UncheckedRunnable run) {
        return run;
    }

    public default void afterRun(R info, Optional<Throwable> error) {

    }

}
