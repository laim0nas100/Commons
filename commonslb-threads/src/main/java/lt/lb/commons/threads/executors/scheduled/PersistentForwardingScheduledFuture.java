package lt.lb.commons.threads.executors.scheduled;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;
import lt.lb.commons.threads.ForwardingScheduledFuture;

/**
 * Forwarding scheduled future with persistent cancel state
 *
 * @author laim0nas100
 */
public class PersistentForwardingScheduledFuture<T> implements ForwardingScheduledFuture<T> {

    protected final PersistentCancel<T, ScheduledFuture<T>> persistent = new PersistentCancel<>(new AtomicReference<>());

    public PersistentForwardingScheduledFuture() {
    }

    public void set(ScheduledFuture<T> f) {
        persistent.set(f);
    }

    @Override
    public ScheduledFuture<T> delegate() {
        return persistent.getRef();
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return persistent.cancel(mayInterruptIfRunning);
    }

    @Override
    public boolean isCancelled() {
        return persistent.isCancelled();
    }

    @Override
    public boolean isDone() {
        return persistent.isDone();
    }

    @Override
    public T get() throws InterruptedException, ExecutionException {
        return persistent.get();
    }

    @Override
    public T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        return persistent.get(timeout, unit);
    }

}
