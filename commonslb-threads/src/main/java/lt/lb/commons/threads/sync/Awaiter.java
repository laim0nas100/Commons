package lt.lb.commons.threads.sync;

import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;
import lt.lb.commons.F;
import lt.lb.commons.Nulls;

/**
 *
 * @author laim0nas100
 */
public interface Awaiter {
    
    public static final BooleanSupplier NEVER_EXIT = () -> false;
    public static final BooleanSupplier ALWAYS_EXIT = () -> true;
    
    public static interface AwaiterTime extends Awaiter {
        
        @Override
        public default void await() throws InterruptedException {
            awaitBool(Long.MAX_VALUE - 1, TimeUnit.NANOSECONDS);
        }
        
        @Override
        public default void await(long timeout, TimeUnit unit) throws InterruptedException, TimeoutException {
            if (!awaitBool(timeout, unit)) {
                throw new TimeoutException("Times up!");
            }
        }
        
        @Override
        public default void await(WaitTime time) throws InterruptedException, TimeoutException {
            await(time.time, time.unit);
        }
        
        public default boolean awaitBool(WaitTime time) throws InterruptedException, TimeoutException {
            return awaitBool(time.time, time.unit);
        }
        
        public boolean awaitBool(long timeout, TimeUnit unit) throws InterruptedException;
        
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
     * Waits if necessary for at most the given time for the computation to
     * complete, and then retrieves its result, if available.
     *
     * @param timeout time to wait
     * @throws CancellationException if the computation was cancelled
     * @throws ExecutionException if the computation threw an exception
     * @throws InterruptedException if the current thread was interrupted while
     * waiting
     * @throws TimeoutException if the wait timed out
     */
    default void await(WaitTime time)
            throws InterruptedException, CancellationException, ExecutionException, TimeoutException {
        await(time.time, time.unit);
    }
    
    public static AwaiterTime promote(Awaiter original) {
        Objects.requireNonNull(original);
        if (original instanceof AwaiterTime) {
            return F.cast(original);
        }
        return new AwaiterTime() {
            @Override
            public boolean awaitBool(long timeout, TimeUnit unit) throws InterruptedException {
                try {
                    original.await(timeout, unit);
                } catch (CancellationException | ExecutionException | TimeoutException ignore) {
                    return false;
                }
                return true;
            }
            
            @Override
            public void await() throws InterruptedException {
                try {
                    original.await();
                } catch (CancellationException | ExecutionException ignore) {
                }
            }
            
        };
    }

    /**
     * Creates simple {@link Awaiter} from given future.
     *
     * @param fut
     * @return
     */
    public static Awaiter fromFuture(Future fut) {
        Objects.requireNonNull(fut);
        return new Awaiter() {
            @Override
            public void await() throws InterruptedException, ExecutionException {
                try {
                    fut.get();
                } catch (CancellationException dicard) {
                    
                }
            }
            
            @Override
            public void await(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
                
                try {
                    fut.get(timeout, unit);
                } catch (CancellationException dicard) {
                    
                }
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
        return compositeTime(NEVER_EXIT, awaiters).awaitBool(time, unit);
    }

    /**
     * Invokes {@link AwaiterTime#awaitBool(long, java.util.concurrent.TimeUnit)
     * } for each {@link AwaiterTime}, while decrementing remaining time.
     * Returns on first {@code false} occurrence or after every invocation or
     * positive fast exit condition
     *
     * @param awaiters
     * @param fastExit fast exit condition
     * @return
     */
    public static Awaiter composite(BooleanSupplier fastExit, Iterable<Awaiter> awaiters) {
        Nulls.requireNonNulls(awaiters, fastExit);
        return new Awaiter() {
            @Override
            public void await() throws InterruptedException, CancellationException, ExecutionException {
                if (fastExit.getAsBoolean()) {
                    return;
                }
                for (Awaiter a : awaiters) {
                    a.await();
                    if (fastExit.getAsBoolean()) {
                        return;
                    }
                }
            }
            
            @Override
            public void await(long timeout, TimeUnit unit) throws InterruptedException, CancellationException, ExecutionException, TimeoutException {
                long nanos = TimeUnit.NANOSECONDS.convert(timeout, unit);
                if (fastExit.getAsBoolean()) {
                    return;
                }
                long now = System.nanoTime();
                for (Awaiter a : awaiters) {
                    if (nanos <= 0) {
                        return;
                    }
                    a.await(nanos, TimeUnit.NANOSECONDS);
                    if (fastExit.getAsBoolean()) {
                        return;
                    }
                    long after = System.nanoTime();
                    long diff = after - now;
                    now = after;
                    nanos = nanos - diff;
                }
            }
        };
        
    }

    /**
     * Helper for {@linkplain Awaiter#composite(java.lang.Iterable, java.util.function.Supplier)
     * }
     */
    public static Awaiter composite(BooleanSupplier fastExit, AwaiterTime... awaiters) {
        return composite(fastExit, Arrays.asList(awaiters));
    }

    /**
     * Invokes {@link AwaiterTime#awaitBool(long, java.util.concurrent.TimeUnit)
     * } for each {@link AwaiterTime}, while decrementing remaining time.
     * Returns on first {@code false} occurrence or after every invocation or
     * positive fast exit condition
     *
     * @param awaiters
     * @param fastExit fast exit condition
     * @return
     */
    public static AwaiterTime compositeTime(BooleanSupplier fastExit, Iterable<AwaiterTime> awaiters) {
        Nulls.requireNonNulls(awaiters, fastExit);
        return (long timeout, TimeUnit unit) -> {
            long nanos = TimeUnit.NANOSECONDS.convert(timeout, unit);
            if (fastExit.getAsBoolean()) {
                return true;
            }
            
            long now = System.nanoTime();
            for (AwaiterTime a : awaiters) {
                if (nanos <= 0) {
                    return false;
                }
                if (!a.awaitBool(nanos, TimeUnit.NANOSECONDS)) {//time's up
                    return false;
                }
                if (fastExit.getAsBoolean()) {
                    return true;
                }
                long after = System.nanoTime();
                long diff = after - now;
                now = after;
                nanos = nanos - diff;
            }
            return true;
        };
    }

    /**
     * Helper for {@linkplain Awaiter#composite(java.lang.Iterable, java.util.function.Supplier)
     * }
     */
    public static AwaiterTime compositeTime(BooleanSupplier fastExit, AwaiterTime... awaiters) {
        return compositeTime(fastExit, Arrays.asList(awaiters));
    }

    public static AwaiterTime fromLockCondition(ReentrantLock lock, Condition condition) {
        return fromLockCondition(NEVER_EXIT, lock, condition);
    }

    public static AwaiterTime fromLockCondition(BooleanSupplier fastExit, ReentrantLock lock, Condition condition) {
        Nulls.requireNonNulls(fastExit, lock, condition);
        return (long timeout, TimeUnit unit) -> {
            
            if (fastExit.getAsBoolean()) {
                return true;
            }
            lock.lockInterruptibly();
            try {
                return fastExit.getAsBoolean() || condition.await(timeout, unit);
            } finally {
                lock.unlock();
            }
            
        };
    }
}
