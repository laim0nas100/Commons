package lt.lb.commons.threads.sync;

import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

/**
 *
 * @author laim0nas100
 */
public interface Awaiter {

    /**
     * Waits if necessary for the computation to complete, and then retrieves
     * its result.
     *
     * @throws CancellationException if the computation was cancelled
     * @throws ExecutionException if the computation threw an exception
     * @throws InterruptedException if the current thread was interrupted while
     * waiting
     */
    void await() throws InterruptedException, CancellationException, ExecutionException;

    /**
     * Waits if necessary for at most the given time for the computation to
     * complete, and then retrieves its result, if available.
     *
     * @param timeout the maximum time to wait
     * @param unit the time unit of the timeout argument
     * @throws CancellationException if the computation was cancelled
     * @throws ExecutionException if the computation threw an exception
     * @throws InterruptedException if the current thread was interrupted while
     * waiting
     * @throws TimeoutException if the wait timed out
     */
    void await(long timeout, TimeUnit unit)
            throws InterruptedException, CancellationException, ExecutionException, TimeoutException;

    /**
     * Creates simple {@link Awaiter} from given future.
     *
     * @param fut
     * @return
     */
    public static Awaiter formFuture(Future fut) {
        return new Awaiter() {
            @Override
            public void await() throws InterruptedException, ExecutionException {
                fut.get();
            }

            @Override
            public void await(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
                fut.get(timeout, unit);
            }
        };
    }

    /**
     * Creates {@link Awaiter} from given atomic future reference and factory.
     * If reference is empty (contains {@code null}), then uses the factory to
     * create a new future and uses that to make an {@link Awaiter}. If
     * reference is not empty and not {@link Future#isDone()
     * }, then also creates a new future from given factory and uses that to
     * make an {@link Awaiter}. Everything is done atomically, that is, no
     * unfinished future can be replaced, until it is actually done.
     *
     * @param <T>
     * @param atomicRef
     * @param factory
     * @return
     */
    public static <T extends Future> Awaiter fromFutureAtomicReference(AtomicReference<T> atomicRef, Supplier<T> factory) {
        T localFuture = atomicRef.get();
        if (localFuture != null) {
            if (localFuture.isDone()) {
                T newFuture = factory.get();
                if (atomicRef.compareAndSet(localFuture, newFuture)) {
                    return formFuture(newFuture);
                } else {
                    return formFuture(atomicRef.get());
                }
            }
            return formFuture(localFuture);
        } else {
            T newFuture = factory.get();
            if (atomicRef.compareAndSet(null, newFuture)) {
                return formFuture(newFuture);
            } else {
                return formFuture(atomicRef.get());
            }

        }
    }
}
