package lt.lb.commons.threads.executors.scheduled;

import java.util.Objects;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import lt.lb.commons.F;
import lt.lb.commons.threads.ExplicitFutureTask;
import lt.lb.commons.threads.FailableRunnableFuture;

/**
 *
 * @author laim0nas100
 */
public class PersistentCancel<T, FUT extends Future<T>> implements FailableRunnableFuture<T> {

    public PersistentCancel(AtomicReference<FUT> reference) {
        this.futureRef = Objects.requireNonNull(reference);
    }

    public PersistentCancel(FUT future) {
        this(new AtomicReference<>(future));
    }

    protected final AtomicInteger state = new AtomicInteger(0);// 0 - not, 1 - failed excplicitly, 2 - cancelled, 3 - cancelled with interrupts, 
    protected final AtomicReference<FUT> futureRef;
    protected final AtomicReference<Throwable> exception = new AtomicReference<>();

    public void set(FUT f) {
        futureRef.set(f);
        if (f != null) {
            int get = state.get();
            if (get == 1 && f instanceof ExplicitFutureTask) {
                ExplicitFutureTask task = F.cast(f);
                task.setException(exception.get());
            } else if (get > 0) {
                f.cancel(get > 2);
            }

        }
    }

    public boolean compareAndSet(FUT expecting, FUT replacing) {
        if (futureRef.compareAndSet(expecting, replacing)) {
            if (replacing != null) {
                int get = state.get();
                if (get == 1 && replacing instanceof ExplicitFutureTask) {
                    ExplicitFutureTask task = F.cast(replacing);
                    task.setException(exception.get());
                } else if (get > 0) {
                    replacing.cancel(get > 2);
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
        if (state.compareAndSet(0, mayInterruptIfRunning ? 3 : 2)) {
            Future future = futureRef.get();
            if (future != null) {
                return future.cancel(mayInterruptIfRunning);
            }
        }
        return false;
    }

    public boolean failed(Throwable error) {
        if (state.compareAndSet(0, 1)) {
            exception.set(error);
            Future replacing = getRef();
            if (replacing == null) {
                return true;
            }
            
            if (replacing instanceof FailableRunnableFuture) {
                FailableRunnableFuture task = F.cast(replacing);
                task.setException(exception.get());
            } else {
                replacing.cancel(true);
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean isCancelled() {
        if (state.get() > 0) {
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
        if (state.get() > 0) {
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
        int get = state.get();
        if (get > 2) {
            Throwable ex = exception.get();
            if (ex instanceof ExecutionException) {
                throw (ExecutionException) ex;
            } else {
                throw new ExecutionException("Task explicitly failed", ex);
            }

        } else if (get > 0) {
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
        int get = state.get();
        if (get > 2) {
            Throwable ex = exception.get();
            if (ex instanceof ExecutionException) {
                throw (ExecutionException) ex;
            } else {
                throw new ExecutionException("Task explicitly failed", ex);
            }

        } else if (get > 0) {
            throw new CancellationException();
        }
        FUT future = futureRef.get();
        if (future != null) {
            return future.get(timeout, unit);
        }
        throw new IllegalStateException("FutureRef is not set");
    }

    @Override
    public void setException(Throwable t) {
        failed(t);
    }

    @Override
    public void run() {
        if (state.get() > 0) {
            return;
        }
        FUT ref = getRef();
        if (ref instanceof Runnable) {
            Runnable r = F.cast(ref);
            r.run();
        }
    }

}
