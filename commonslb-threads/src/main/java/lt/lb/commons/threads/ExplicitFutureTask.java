package lt.lb.commons.threads;

import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 *
 * @author laim0nas100
 */
public class ExplicitFutureTask<T> extends FutureTask<T> implements FailableRunnableFuture<T> {

    protected AtomicBoolean NEW = new AtomicBoolean(true);

    public ExplicitFutureTask(Callable<T> callable) {
        super(callable);
    }

    public ExplicitFutureTask(Runnable runnable, T result) {
        super(runnable, result);
    }

    @Override
    public void setException(Throwable t) {
        super.setException(t);
    }

    @Override
    public void run() {
        if (NEW.compareAndSet(true, false)) {
            super.run();
        }

    }

    public boolean isNew() {
        return NEW.get();
    }
}
