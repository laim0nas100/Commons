package lt.lb.commons.threads;

import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

/**
 *
 * @author laim0nas100
 */
public class ExplicitFutureTask<T> extends FutureTask<T> implements FailableRunnableFuture<T> {

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
}
