package lt.lb.commons.threads.sync;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import lt.lb.commons.Java;

/**
 *
 * @author laim0nas100 read-write Semaphore lock
 *
 */
public class ReadWriteLock implements Lock {

    protected volatile int readers = 0;
    protected volatile int writers = 0;
    protected volatile int writeReq = 0;

    public synchronized boolean tryLockRead() {
        if (writers + writeReq == 0) {
            readers++;
            return true;
        }
        return false;
    }

    public synchronized boolean tryLockWrite() {
        if (readers + writers + writeReq == 0) {
            writers++;
            return true;
        }
        return false;
    }

    public synchronized boolean tryLockWrite(long millis) throws InterruptedException {
        if (tryLockWrite()) {
            return true;
        }
        if (millis <= 0) {
            return false;
        }

        writeReq++;
        long now = Java.getNanoTime();
        long left = WaitTime.ofMillis(millis).toNanos();
        while (readers + writers > 0) {
            if (left <= 0) {
                return false;// failed to await
            }
            wait(WaitTime.ofNanos(left).toMillis());
            long newNow = Java.getNanoTime();
            long elapsed = (newNow - now);
            left = left - elapsed;

            now = newNow;
        }
        writeReq--;
        writers++;
        return true;
    }

    public synchronized boolean tryLockRead(long millis) throws InterruptedException {
        if (tryLockRead()) {
            return true;
        }
        if (millis <= 0) {
            return false;
        }

        long now = Java.getNanoTime();
        long left = WaitTime.ofMillis(millis).toNanos();
        while (writeReq + writers > 0) {
            if (left <= 0) {
                return false;// failed to await
            }
            wait(WaitTime.ofNanos(left).toMillis());
            long newNow = Java.getNanoTime();
            long elapsed = (newNow - now);
            left = left - elapsed;
            now = newNow;
        }
        readers++;
        return true;
    }

    public synchronized void lockRead() throws InterruptedException {
        while (writers + writeReq > 0) {
            wait();
        }
        readers++;
    }

    public synchronized void unlockRead() {
        readers--;
        notifyAll();
    }

    public synchronized void lockWrite() throws InterruptedException {
        writeReq++;
        while (readers + writers > 0) {
            wait();
        }
        writeReq--;
        writers++;
    }

    public synchronized void unlockWrite() {
        writers--;
        notifyAll();
    }

    public boolean isBeingWritten() {
        return writers > 0;
    }

    public boolean isBeingRead() {
        return readers > 0;
    }

    @Override
    public void lock() {
        for (;;) {
            try {
                if (tryLockWrite(60_000)) {// retry every minute
                    return;
                }
            } catch (InterruptedException ex) {// disregard, go again
            }
        }
    }

    @Override
    public void lockInterruptibly() throws InterruptedException {
        lockWrite();
    }

    @Override
    public boolean tryLock() {
        return tryLockWrite();
    }

    @Override
    public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
        return tryLockWrite(WaitTime.of(time, unit).toMillisAssert());
    }

    @Override
    public void unlock() {
        unlockWrite();
    }

    /**
     * Not supported.
     *
     * @return
     */
    @Override
    public Condition newCondition() {
        throw new UnsupportedOperationException("Not supported.");
    }
}
