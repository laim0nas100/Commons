package lt.lb.commons.threads.service;

import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import lt.lb.commons.threads.sync.AtomicMap;
import lt.lb.uncheckedutils.Checked;
import lt.lb.uncheckedutils.func.UncheckedRunnable;

/**
 *
 * @author laim0nas100
 */
public interface ScheduledTaskExecutor<P> {

    public static enum FailedSubmitCase {
        UNIQUE_RUN, UNIQUE_SUBMIT
    }

    public static enum Occupy {
        FREE, RUNNING, SUBMITTED
    }


    public ScheduledExecutorService getScheduler();

    public ExecutorService getExecutor();
    
    public AtomicMap<P,Occupy> getAtomicMap();

    public default void schedulePeriodically(boolean unique, long first, long period, TimeUnit unit, P name, UncheckedRunnable run) {
        getScheduler().scheduleAtFixedRate(() -> {
            if (unique) {
                submitUnique(name, run);
            } else {
                submit(name, run);
            }

        }, first, period, unit);
    }

    public default void schedule(boolean unique, long first, TimeUnit unit, P name, UncheckedRunnable run) {
        getScheduler().schedule(() -> {
            if (unique) {
                submitUnique(name, run);
            } else {
                submit(name, run);
            }

        }, first, unit);
    }

    public default void submit(P name, UncheckedRunnable run) {
        getExecutor().submit(() -> {
            run(name, run);
        });
    }

    public default void run(P name, UncheckedRunnable run) {
        afterRun(name, Checked.checkedRun(beforeRun(name, run)));
    }

    public UncheckedRunnable beforeRun(P name, UncheckedRunnable run);

    public void afterRun(P name, Optional<Throwable> error);

    public void failedToSubmit(FailedSubmitCase failedCase, P name, UncheckedRunnable run);

    public default void runUnique(P name, UncheckedRunnable run) {
        Occupy state = atomicState(name);

        if (state == Occupy.SUBMITTED) {
            run(name, run);
            getAtomicMap().changeIfPresent(name, b -> Occupy.FREE);
        } else {
            failedToSubmit(FailedSubmitCase.UNIQUE_RUN, name, run);
        }

    }

    public default Occupy atomicState(P name) {
        return getAtomicMap().changeSupplyIfAbsent(name, Occupy.FREE, b -> {
            if (b == Occupy.FREE) {
                return Occupy.SUBMITTED;
            }
            if (b == Occupy.SUBMITTED) {
                return Occupy.RUNNING;
            }
            return b;
        });
    }

    public default void submitUnique(P name, UncheckedRunnable run) {

        Occupy state = atomicState(name);

        if (state == Occupy.SUBMITTED) {
            getExecutor().submit(() -> {
                run(name, run);
                getAtomicMap().changeIfPresent(name, b -> Occupy.FREE);
            });
        } else {
            failedToSubmit(FailedSubmitCase.UNIQUE_SUBMIT, name, run);
        }

    }

}
