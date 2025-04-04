package lt.lb.commons.threads.sync;

import java.util.concurrent.TimeUnit;

/**
 *
 * @author laim0nas100
 */
public class ConditionalWait {

    private volatile boolean keepWaiting = false;
    private final Object lock = new Object();

    public void conditionalWait() {
        if (!keepWaiting) {
            return;
        }
        try {
            synchronized (lock) {
                while (keepWaiting) {
                    lock.wait();
                }
            }

        } catch (InterruptedException e) {
        }
    }

    public void conditionalWait(WaitTime time) {
        if (!keepWaiting) {
            return;
        }
        long waitTime = time.convert(TimeUnit.MILLISECONDS).time;
        try {
            synchronized (lock) {
                while (keepWaiting) {
                    lock.wait(waitTime);
                }
            }

        } catch (InterruptedException ex) {
        }

    }

    public boolean isInWait() {
        return keepWaiting;
    }

    public void wakeUp() {
        keepWaiting = false;
        synchronized (lock) {
            lock.notifyAll();
        }

    }

    public void requestWait() {
        keepWaiting = true;
    }
}
