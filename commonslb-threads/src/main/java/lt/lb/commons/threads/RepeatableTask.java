package lt.lb.commons.threads;

import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import lt.lb.uncheckedutils.Checked;
import lt.lb.uncheckedutils.NestedException;
/**
 *
 * @author laim0nas100
 */
public class RepeatableTask implements Runnable {

    private AtomicBoolean ready = new AtomicBoolean(true);
    private Callable call;

    public RepeatableTask(Runnable task) {
        this.call = Executors.callable(task, null);
    }

    public RepeatableTask(Callable task) {
        this.call = task;
    }

    @Override
    public void run() {
        if (ready.compareAndSet(true, false)) {
            Optional<Throwable> checkedRun = Checked.checkedRun(() -> {
                call.call();
            });
            ready.set(true);
            if (checkedRun.isPresent()) {
                throw NestedException.of(checkedRun.get());
            }
        }
    }

}
