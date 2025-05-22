package lt.lb.commons.threads.sync;

import java.util.Objects;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
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

    public static interface AwaiterTimeFunction {

        public boolean await(long timeout, TimeUnit unit) throws InterruptedException;
    }

    public static interface AwaiterTime extends Awaiter {

        @Override
        public default void await() throws InterruptedException {
            awaitBool(Long.MAX_VALUE - 1, TimeUnit.NANOSECONDS);
        }

        @Override
        public void await(long timeout, TimeUnit unit) throws InterruptedException, TimeoutException;

        public default boolean awaitBool(long timeout, TimeUnit unit) throws InterruptedException {
            try {
                await(timeout, unit);
                return true;
            } catch (TimeoutException ex) {
                return false;
            }
        }

    }

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
    public static Awaiter fromFuture(Future fut) {
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
     * unfinished future can be replaced until it is actually done.
     *
     * @param <T>
     * @param atomicRef
     * @param factory
     * @return
     */
    public static <T extends Future> Awaiter fromFutureAtomicReference(AtomicReference<T> atomicRef, Supplier<T> factory) {
        return fromFuture(atomicFutureResolve(atomicRef, factory));
    }

    public static <T extends Future> T atomicFutureResolve(AtomicReference<T> atomicRef, Supplier<T> factory) {
        T localFuture = atomicRef.get();
        if (localFuture != null) {
            if (localFuture.isDone()) {
                T newFuture = factory.get();
                if (atomicRef.compareAndSet(localFuture, newFuture)) {
                    return newFuture;
                } else {
                    return atomicRef.get();
                }
            }
            return localFuture;
        } else {
            T newFuture = factory.get();
            if (atomicRef.compareAndSet(null, newFuture)) {
                return newFuture;
            } else {
                return atomicRef.get();
            }

        }
    }

    public static <T extends CompletableFuture> Awaiter fromCompletableFuture(AtomicReference<CompletableFuture> atomicRef) {
        return fromFutureAtomicReference(atomicRef, CompletableFuture::new);
    }

    /**
     * Creates an {@link AwaiterTime> from supplied {@link AwaiterTimeFunction}.
     *
     * @param func
     * @return
     */
    public static AwaiterTime fromFunction(AwaiterTimeFunction func) {
        Objects.requireNonNull(func, "AwaiterTimeFunction is null");

        return new AwaiterTime() {
            @Override
            public void await(long timeout, TimeUnit unit) throws InterruptedException, TimeoutException {
                if (!func.await(timeout, unit)) {
                    throw new TimeoutException();
                }
            }

            @Override
            public boolean awaitBool(long timeout, TimeUnit unit) throws InterruptedException {
                return func.await(timeout, unit);
            }

        };
    }

    /**
     * Invokes {@link Awaiter#await() } for each {@link Awaiter}.
     *
     * @param awaiters
     * @throws InterruptedException
     * @throws CancellationException
     * @throws ExecutionException
     */
    public static void sharedAwait(Iterable<Awaiter> awaiters) throws InterruptedException, CancellationException, ExecutionException {
        Objects.requireNonNull(awaiters, "Awaiters are null");
        for (Awaiter a : awaiters) {
            a.await();
        }
    }

    /**
     * Invokes {@link Awaiter#await(long, java.util.concurrent.TimeUnit) } for
     * each {@link Awaiter}, while decrementing remaining time
     *
     * @param awaiters
     * @param time
     * @param unit
     * @throws InterruptedException
     * @throws CancellationException
     * @throws ExecutionException
     * @throws TimeoutException
     */
    public static void sharedAwait(Iterable<Awaiter> awaiters, long time, TimeUnit unit) throws InterruptedException, CancellationException, ExecutionException, TimeoutException {
        Objects.requireNonNull(awaiters, "Awaiters are null");
        long nanos = TimeUnit.NANOSECONDS.convert(time, unit);
        long now = System.nanoTime();
        for (Awaiter a : awaiters) {
            if (nanos <= 0) {
                throw new TimeoutException("Times up!");
            }

            a.await(nanos, TimeUnit.NANOSECONDS);
            long after = System.nanoTime();
            long diff = after - now;
            now = after;
            nanos = nanos - diff;
        }
    }

    /**
     * Invokes {@link AwaiterTime#await(long, java.util.concurrent.TimeUnit) }
     * for each {@link AwaiterTime}, while decrementing remaining time
     *
     * @param awaiters
     * @param time
     * @param unit
     * @throws InterruptedException
     * @throws CancellationException
     * @throws TimeoutException
     */
    public static void sharedAwaitTime(Iterable<AwaiterTime> awaiters, long time, TimeUnit unit) throws InterruptedException, TimeoutException {
        Objects.requireNonNull(awaiters, "Awaiters are null");
        long nanos = TimeUnit.NANOSECONDS.convert(time, unit);
        long now = System.nanoTime();
        for (AwaiterTime a : awaiters) {
            if (nanos <= 0) {
                throw new TimeoutException("Times up!");
            }

            a.await(nanos, TimeUnit.NANOSECONDS);
            long after = System.nanoTime();
            long diff = after - now;
            now = after;
            nanos = nanos - diff;
        }
    }

    /**
     * Invokes {@link AwaiterTime#awaitBool(long, java.util.concurrent.TimeUnit)
     * } for each {@link AwaiterTime}, while decrementing remaining time.
     * Returns on first {@code false} occurrence or after every invocation.
     *
     * @param awaiters
     * @param time
     * @param unit
     * @return
     * @throws InterruptedException
     * @throws CancellationException
     */
    public static boolean sharedAwaitTimeBool(Iterable<AwaiterTime> awaiters, long time, TimeUnit unit) throws InterruptedException {
        Objects.requireNonNull(awaiters, "Awaiters are null");
        long nanos = TimeUnit.NANOSECONDS.convert(time, unit);
        long now = System.nanoTime();
        for (AwaiterTime a : awaiters) {
            if (nanos <= 0) {
                return false;
            }
            if (!a.awaitBool(nanos, TimeUnit.NANOSECONDS)) {
                return false;
            }
            long after = System.nanoTime();
            long diff = after - now;
            now = after;
            nanos = nanos - diff;
        }
        return true;
    }
}
