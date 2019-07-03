package lt.lb.commons.threads;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import lt.lb.commons.F;
import lt.lb.commons.threads.sync.WaitTime;

/**
 *
 * @author Laimonas Beniušis
 */
public class RunnableDecorators {

    /**
     * Decorates Runnable to be interrupted after a set amount of time.
     * If given runnable ignores interrupts, then we can't do anything.
     * @param time
     * @param run
     * @return
     */
    public static Runnable withTimeout(WaitTime time, Runnable run) {
        return () -> {
            ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
            Thread toCancel = Thread.currentThread();
            AtomicBoolean isDone = new AtomicBoolean(false);
            Runnable interrupter = () -> {
                if (isDone.get()) {
                    return;
                }
                toCancel.interrupt();
            };
            service.schedule(interrupter, time.time, time.unit);
            F.checkedRun(run);
            isDone.set(true);
            service.shutdownNow();

        };

    }
}
