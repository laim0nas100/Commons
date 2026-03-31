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
    protected final FutureTask innerFuture;

    public ExplicitFutureTask(Callable<T> callable) {
        super(callable);
        if (callable instanceof FutureTask) {
            innerFuture = (FutureTask) callable;
        } else {
            innerFuture = null;
        }
    }

    public ExplicitFutureTask(Runnable runnable, T result) {
        super(runnable, result);
        if (runnable instanceof FutureTask) {
            innerFuture = (FutureTask) runnable;
        } else {
            innerFuture = null;
        }
    }

    @Override
    public void setException(Throwable t) {
        if (NEW.compareAndSet(true, false)) {
            super.setException(t);
            if (innerFuture != null) {
                innerFuture.cancel(false);
            }
        }

    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        if (innerFuture != null) {
            innerFuture.cancel(mayInterruptIfRunning);
        }
        return super.cancel(mayInterruptIfRunning);
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
