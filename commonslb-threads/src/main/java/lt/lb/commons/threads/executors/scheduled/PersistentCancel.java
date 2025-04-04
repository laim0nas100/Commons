package lt.lb.commons.threads.executors.scheduled;

import java.util.Objects;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 *
 * @author laim0nas100
 */
public class PersistentCancel<T, FUT extends Future<T>> implements Future<T> {

    public PersistentCancel(AtomicReference<FUT> reference) {
        this.futureRef = Objects.requireNonNull(reference);
    }

    public PersistentCancel(FUT future) {
        this(new AtomicReference<>(future));
    }

    protected final AtomicInteger cancel = new AtomicInteger(0);// 0 - not, 1 - cancelled, 2 - cancelled with interrupts
    protected final AtomicReference<FUT> futureRef;

    public void set(FUT f) {
        futureRef.set(f);
        if (f != null) {
            int get = cancel.get();
            if (get > 0) {
                f.cancel(get > 1);
            }

        }
    }

    public boolean compareAndSet(FUT expecting, FUT replacing) {
        if (futureRef.compareAndSet(expecting, replacing)) {
            if (replacing != null) {
                int get = cancel.get();
                if (get > 0) {
                    replacing.cancel(get > 1);
                }
            }
            return true;
        }
        return false;
    }

    public FUT getRef() {
        return futureRef.get();
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        if (cancel.compareAndSet(0, mayInterruptIfRunning ? 2 : 1)) {
            Future future = futureRef.get();
            if (future != null) {
                return future.cancel(mayInterruptIfRunning);
            }
        }
        return false;
    }

    @Override
    public boolean isCancelled() {
        if (cancel.get() > 0) {
            return true;
        }
        Future future = futureRef.get();
        if (future != null) {
            return future.isCancelled();
        }
        return false;
    }

    @Override
    public boolean isDone() {
        if (cancel.get() > 0) {
            return true;
        }
        FUT future = futureRef.get();
        if (future != null) {
            return future.isDone();
        }
        return false;
    }

    @Override
    public T get() throws InterruptedException, ExecutionException {
        if (cancel.get() > 0) {
            throw new CancellationException();
        }
        FUT future = futureRef.get();
        if (future != null) {
            return future.get();
        }
        throw new IllegalStateException("FutureRef is not set");
    }

    @Override
    public T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        if (cancel.get() > 0) {
            throw new CancellationException();
        }
        FUT future = futureRef.get();
        if (future != null) {
            return future.get(timeout, unit);
        }
        throw new IllegalStateException("FutureRef is not set");
    }

}
