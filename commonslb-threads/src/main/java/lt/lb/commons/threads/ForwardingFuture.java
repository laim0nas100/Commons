package lt.lb.commons.threads;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 *
 * @author laim0nas100
 */
public interface ForwardingFuture<T> extends Future<T> {

    public Future<T> delegate();

    @Override
    public default boolean cancel(boolean mayInterruptIfRunning) {
        return delegate().cancel(mayInterruptIfRunning);
    }

    @Override
    public default boolean isCancelled() {
        return delegate().isCancelled();
    }

    @Override
    public default boolean isDone() {
        return delegate().isDone();
    }

    @Override
    public default T get() throws InterruptedException, ExecutionException {
        return delegate().get();
    }

    @Override
    public default T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        return delegate().get(timeout, unit);
    }

}
