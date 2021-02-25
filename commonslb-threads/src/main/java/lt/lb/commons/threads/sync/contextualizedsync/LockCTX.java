package lt.lb.commons.threads.sync.contextualizedsync;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import lt.lb.commons.Java;
import lt.lb.commons.threads.sync.WaitTime;
import lt.lb.fastid.FastID;
import lt.lb.fastid.FastIDGen;

/**
 *
 * @author laim0nas100
 */
public class LockCTX {

    private static final FastIDGen lockCtxGen = new FastIDGen();
    /**
     * Lock creation time using {@link Java#getNanoTime() }
     */
    public final long creationNanos = Java.getNanoTime();
    /**
     * How many times to retry when encountering a locked context.
     */
    public final int timesToRetry;
    /**
     * How much time to wait after retry when encountering a locked context.
     */
    public final WaitTime waitAtComplete;
    /**
     * Thread that uses this lock.
     */
    public final Thread thread = Thread.currentThread();
    /**
     * Lock key used in {@link ContextualizedSync}
     */
    public final Object key;

    /**
     * Lock UUID.
     */
    public final FastID computedToken = lockCtxGen.getAndIncrement();

    protected LockCTX primaryLock;
    protected AtomicBoolean locked = new AtomicBoolean(false);
    protected AtomicBoolean unlocked = new AtomicBoolean(false);
    protected CompletableFuture completable = new CompletableFuture();

    protected boolean expired(Duration diff, long now) {
        return now - creationNanos > diff.toNanos();
    }

    /**
     * The lock is expired if it was created earlier than given duration allows,
     * measured from now.
     *
     * @param diff
     * @return
     */
    public boolean expired(Duration diff) {
        return expired(diff, Java.getNanoTime());
    }

    public LockCTX(int timesToRetry, WaitTime time, Object k) {
        this.key = Objects.requireNonNull(k);
        this.timesToRetry = Math.max(1, timesToRetry);
        this.waitAtComplete = Objects.requireNonNull(time);
    }

    /**
     * If lock has been used in {@link ContextualizedSync}
     *
     * @return
     */
    public boolean isUsed() {
        return completable.isDone() || locked.get() || isNotPrimary();
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.computedToken);
    }

    @Override
    public String toString() {
        return "LockCTX " + computedToken + " key:" + key;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final LockCTX other = (LockCTX) obj;
        if (!Objects.equals(this.computedToken, other.computedToken)) {
            return false;
        }
        return true;
    }

    /**
     * If lock was ever used
     *
     * @return
     */
    public boolean wasLocked() {
        if (isNotPrimary()) {
            return primaryLock.wasLocked();
        }
        return locked.get();
    }

    /**
     * If used lock was unlocked successfully.
     *
     * @return
     */
    public boolean wasUnlocked() {
        if (isNotPrimary()) {
            return primaryLock.wasUnlocked();
        }
        return unlocked.get();
    }

    /**
     * If lock ever was stuck and then had to be cancelled
     *
     * @return
     */
    public boolean wasCancelledStuck() {
        if (isNotPrimary()) {
            return primaryLock.wasCancelledStuck();
        }
        return completable.isCancelled();
    }

    /**
     * If lock was never actually used for locking, but just used under another
     * "main" lock with same key in the same thread. Doesn't happen when using
     * locks manually.
     *
     * @return
     */
    public boolean isNotPrimary() {
        return primaryLock != null;
    }

}
