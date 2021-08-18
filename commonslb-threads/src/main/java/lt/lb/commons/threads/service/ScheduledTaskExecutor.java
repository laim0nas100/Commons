package lt.lb.commons.threads.service;

import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import lt.lb.commons.threads.Futures;
import lt.lb.commons.threads.sync.AtomicMap;
import lt.lb.uncheckedutils.Checked;
import lt.lb.uncheckedutils.PassableException;
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

    public AtomicMap<P, Occupy> getAtomicMap();

    public default ScheduledFuture<?> schedulePeriodically(boolean unique, long first, long period, TimeUnit unit, P name, UncheckedRunnable run) {
        return getScheduler().scheduleAtFixedRate(() -> {
            if (unique) {
                submitUnique(name, run);
            } else {
                submit(name, run);
            }

        }, first, period, unit);
    }

    public default ScheduledFuture<?> schedule(boolean unique, long first, TimeUnit unit, P name, UncheckedRunnable run) {
        return getScheduler().schedule(() -> {
            if (unique) {
                submitUnique(name, run);
            } else {
                submit(name, run);
            }

        }, first, unit);
    }

    public default Future<?> submit(P name, UncheckedRunnable run) {
        return getExecutor().submit(() -> {
            run(false, name, run);
        });
    }

    public default Optional<Throwable> run(boolean changing, P name, UncheckedRunnable run) {
        Optional<Throwable> checkedRun = Checked.checkedRun(beforeRun(name, run));
        if (changing) {
            getAtomicMap().changeIfPresent(name, b -> Occupy.FREE);
        }
        afterRun(name, checkedRun);
        return checkedRun;
    }

    public UncheckedRunnable beforeRun(P name, UncheckedRunnable run);

    public void afterRun(P name, Optional<Throwable> error);

    public void failedToSubmit(FailedSubmitCase failedCase, P name, UncheckedRunnable run);

    public default Optional<Throwable> runUnique(P name, UncheckedRunnable run) {
        Occupy state = atomicState(name);

        if (state == Occupy.SUBMITTED) {
            return run(true, name, run);
        } else {
            failedToSubmit(FailedSubmitCase.UNIQUE_RUN, name, run);
            return Optional.of(new PassableException("Failed to submit " + name));
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

    public default Future<?> submitUnique(P name, UncheckedRunnable run) {

        Occupy state = atomicState(name);

        if (state == Occupy.SUBMITTED) {
            return getExecutor().submit(() -> {
                run(true, name, run);
            });
        } else {
            failedToSubmit(FailedSubmitCase.UNIQUE_SUBMIT, name, run);
            return Futures.exceptional(() -> new PassableException("Failed to submit " + name));
        }

    }

}
