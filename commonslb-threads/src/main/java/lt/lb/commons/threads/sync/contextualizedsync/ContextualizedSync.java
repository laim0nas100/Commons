package lt.lb.commons.threads.sync.contextualizedsync;

import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;
import lt.lb.commons.Java;
import lt.lb.commons.containers.values.BooleanValue;
import lt.lb.commons.threads.sync.AtomicMap;
import lt.lb.commons.threads.sync.AtomicMapDefault;
import lt.lb.commons.threads.sync.WaitTime;
import lt.lb.uncheckedutils.Checked;
import lt.lb.uncheckedutils.SafeOpt;
import lt.lb.uncheckedutils.func.UncheckedRunnable;
import lt.lb.uncheckedutils.func.UncheckedSupplier;

/**
 *
 * A way to contextually use {@code synchronized} functionality across different
 * contexts. If any context gets "stuck", it can be forcefully removed. Supports
 * nesting in the same thread if same context is given.
 *
 * @author laim0nas100
 */
public class ContextualizedSync {

    public static class ContextSyncException extends Exception {

        public final LockCTX lock;

        public ContextSyncException(LockCTX lock, String message) {
            super(message);
            this.lock = lock;
        }

        public ContextSyncException(LockCTX lock, String message, Throwable cause) {
            super(message, cause);
            this.lock = lock;
        }

    }

    public ContextualizedSync(int timesToRetry, WaitTime waitAtComplete) {
        this(timesToRetry, waitAtComplete, new ConcurrentHashMap<>());
    }

    public ContextualizedSync(int timesToRetry, WaitTime waitAtComplete, ConcurrentMap<Object, LockCTX> map) {
        this.defaultRetries = timesToRetry;
        this.defaultWait = waitAtComplete;
        this.atomic = new AtomicMapDefault<>(map);
    }

    public ContextualizedSync() {
        this(5, WaitTime.ofSeconds(5));
    }

    protected final int defaultRetries;
    protected final WaitTime defaultWait;

    /**
     * Get current lock snapshot
     *
     * @return
     */
    public List<LockCTX> getLocks() {
        return atomic.getValues();
    }

    /**
     * Get expired lock snapshot
     *
     * @param time
     * @return
     */
    public List<LockCTX> getExpiredLocks(WaitTime time) {
        return getExpiredLocks(time.toDuration());
    }

    /**
     * Get expired lock snapshot
     *
     * @param duration
     * @return
     */
    public List<LockCTX> getExpiredLocks(Duration duration) {
        long now = Java.getNanoTime();
        return getLocks().stream().filter(l -> l != null).filter(l -> l.expired(duration, now)).collect(Collectors.toList());
    }

    /**
     * Get current locked context keys snapshot.
     *
     * @return
     */
    public List getKeys() {
        return atomic.getKeys();
    }

    protected AtomicMap<Object, LockCTX> atomic;

    /**
     * Manually control lock. This is done automatically by
     * {@link ContextualizedSync#doSynchronized}
     *
     * @param lock
     * @return
     */
    public boolean tryLock(LockCTX lock) {
        return establish(lock) == null;
    }

    /**
     * Manually control lock. This is done automatically by
     * {@link ContextualizedSync#doSynchronized}
     *
     * @param lock
     * @return
     */
    public boolean tryUnlock(LockCTX lock) {
        return destablish(lock) == null;
    }

    protected LockCTX destablish(final LockCTX lock) {
        if (!lock.isUsed()) {
            throw new IllegalArgumentException("Trying to unlock with unused: " + lock);
        }
        return atomic.changeIfPresent(lock.key, (presentLock) -> {
            if (Objects.equals(presentLock, lock)) {
                //unlock
                if (!presentLock.unlocked.compareAndSet(false, true)) {
                    throw new IllegalStateException("Failed to unlock, after synchronized execution: " + lock);
                }
                presentLock.completable.complete(0); // complete gracefully
                return null; // removed
            }
            return presentLock; //someone else allready removed
        });
    }

    protected LockCTX establish(final LockCTX lock) {
        if (lock.isUsed()) {
            throw new IllegalArgumentException("Can't establish lock that is used: " + lock);
        }
        LockCTX computedLock = atomic.changeIfAbsent(lock.key, () -> {
            if (!lock.locked.compareAndSet(false, true)) {
                throw new IllegalStateException("Failed to lock, but there is a free space in lock context: " + lock);
            }
            return lock;
        });
        if (Objects.equals(lock, computedLock)) {
            return null;
        }
        return computedLock;
    }

    /**
     * Creates and tries to establish default lock based on given context
     * parameter and executes given call. Returns saved exception, if such had
     * happened.
     *
     * @param context
     * @param runnable
     * @return
     */
    public SafeOpt<Throwable> doSynchronized(Object context, UncheckedRunnable runnable) {
        return doSynchronized(new LockCTX(defaultRetries, defaultWait, context), runnable);
    }

    /**
     * Try to establish given lock and executes given call. Returns saved
     * exception, if such had happened.
     *
     * @param lock
     * @param runnable
     * @return
     */
    public SafeOpt<Throwable> doSynchronized(LockCTX lock, UncheckedRunnable runnable) {
        Objects.requireNonNull(lock);
        Objects.requireNonNull(runnable);
        return doSynchronized(lock, () -> {
            runnable.run();
            return null;
        }).getError();
    }

    /**
     * Creates and tries to establish default lock based on given context
     * parameter and executes given call. Returns saved exception, if such had
     * happened.
     *
     * @param context
     * @param call
     * @return
     */
    public <T> SafeOpt<T> doSynchronized(Object context, UncheckedSupplier<T> call) {
        return doSynchronized(new LockCTX(defaultRetries, defaultWait, context), call);
    }

    /**
     * Try to establish given lock and executes given call. Returns result with
     * saved exception, if such had happened.
     *
     * @param <T>
     * @param lock
     * @param call
     * @return
     */
    public <T> SafeOpt<T> doSynchronized(LockCTX lock, UncheckedSupplier<T> call) {
        Objects.requireNonNull(lock);
        Objects.requireNonNull(call);
        if (lock.isUsed()) {
            return SafeOpt.error(new ContextSyncException(lock, "Lock:" + lock + " is used, therefore cannot be used again"));
        }

        atomic.changeIfPresent(lock.key, (presentLock) -> {
            if (Objects.equals(lock.thread, presentLock.thread)) {
                lock.primaryLock = presentLock;
            }
            return presentLock;
        });
        if (lock.isNotPrimary()) {
            return Checked.checkedCall(call);
        }

        try {
            LockCTX establish = establish(lock);

            int times = lock.timesToRetry;
            while (establish != null) { // null means established
                if (times < 0) {
                    return SafeOpt.error(new ContextSyncException(lock, "Failed to lock locally with " + lock));
                }
                times--;
                try {
                    establish.completable.get(lock.waitAtComplete.time, lock.waitAtComplete.unit); // this should be fast
                } catch (InterruptedException | ExecutionException ex) {
                    return SafeOpt.error(new ContextSyncException(lock, "Caught inside ContextualizedSync lock wait block, failed to enter with " + lock, ex));
                } catch (TimeoutException ex) {
                    // do nothing, just wait again
                }
                establish = establish(lock);
            }

            SafeOpt<T> checkedCall = Checked.checkedCall(call);
            destablish(lock);

            return checkedCall;
        } catch (Throwable th) {
            return SafeOpt.error(th);
        }

    }

    /**
     * Forcefully (optionally interrupting thread that has this lock) cancel and remove
     * lock, if lock is present and was actually locked.Doesn't get "unlocked"
     * status when removed like this, but no longer occupies given context.
     *
     * @param lock
     * @param interrupt
     * @return
     */
    public boolean forceRemoveLock(LockCTX lock, boolean interrupt) {

        Objects.requireNonNull(lock);
        if (!lock.isUsed()) {
            throw new IllegalArgumentException("Trying to remove unused :" + lock);
        }
        BooleanValue ok = new BooleanValue(false);
        atomic.changeIfPresent(lock.key, (presentLock) -> {
            if (Objects.equals(presentLock, lock)) { // 

                if (!presentLock.wasLocked()) {
                    throw new IllegalStateException(lock + " was never used, but now trying to unstuck it. Check your code.");
                }
                if (presentLock.isNotPrimary()) {
                    ok.set(presentLock.primaryLock.completable.cancel(interrupt));
                } else {
                    ok.set(presentLock.completable.cancel(interrupt));
                }

                if (interrupt) {
                    lock.thread.interrupt();
                }
                return null;
            } else {
                return presentLock;
            }
        });
        return ok.get();
    }
}
