package lt.lb.commons.threads.sync;

import java.util.Objects;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 *
 * @author laim0nas100
 */
public class ConditionalWait {

    private volatile boolean keepWaiting = false;
    private final ReentrantLock lock;
    private final Condition awaiter;

    public ConditionalWait() {
        this(false);
    }

    public ConditionalWait(boolean fair) {
        lock = new ReentrantLock(fair);
        awaiter = lock.newCondition();
    }

    public void conditionalWait() {
        if (!keepWaiting) {
            return;
        }

        try {
            lock.lockInterruptibly();
            while (keepWaiting) {
                awaiter.await();
            }

        } catch (InterruptedException e) {
        } finally {
            lock.unlock();
        }
    }

    public void conditionalWait(WaitTime time) {
        if (!keepWaiting) {
            return;
        }
        Objects.requireNonNull(time);
        try {
            lock.lockInterruptibly();
            while (keepWaiting) {
                awaiter.await(time.time, time.unit);
            }

        } catch (InterruptedException e) {
        } finally {
            lock.unlock();
        }

    }

    public boolean isInWait() {
        return keepWaiting;
    }

    private boolean change(boolean newState) {
        if (keepWaiting == newState) {
            return false;
        }
        lock.lock();
        try {
            keepWaiting = newState;
            if (!newState && lock.hasWaiters(awaiter)) {
                awaiter.signalAll();
            }
        } finally {
            lock.unlock();
        }
        return true;
    }

    public void wakeUp() {
        change(false);
    }

    public void requestWait() {
        change(true);

    }
}
