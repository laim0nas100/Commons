package lt.lb.commons.threads.service;

import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import lt.lb.commons.Java;
import lt.lb.commons.threads.sync.WaitTime;

/**
 * Only allow limited number of requests per given time window
 *
 * @author laim0nas100
 */
public class RequestThrottle {

    protected final WaitTime timeWindow;
    protected final AtomicInteger requestsMade = new AtomicInteger(0);
    protected final int requestsPerWindow;

    protected final long waitNanos;
    protected final AtomicLong lastReset = new AtomicLong(Java.getNanoTime());

    public RequestThrottle(WaitTime timeWindow, int requestsPerWindow) {
        this.timeWindow = Objects.requireNonNull(timeWindow);
        this.requestsPerWindow = Math.max(1, requestsPerWindow);
        waitNanos = timeWindow.convert(TimeUnit.NANOSECONDS).time;
    }

    protected AtomicBoolean inReset = new AtomicBoolean(false);
    protected AtomicInteger whileInReset = new AtomicInteger();

    protected void updateTime(long now) {
        long call = lastReset.get();
        if (now - call >= waitNanos) {
            if (inReset.compareAndSet(false, true)) {
                // while in reset, no one can modify lastRest and requestsMade
                if (lastReset.compareAndSet(call, now)) {
                    requestsMade.set(0);
                }
                inReset.compareAndSet(true, false);
                //exit reset mode, so now we can modify whileInRest, because whileInReset only changes in non-reset mode
                requestsMade.addAndGet(whileInReset.getAndSet(0));
            }
        }
    }

    public boolean request() {
        updateTime(Java.getNanoTime());
        if (inReset.get() && whileInReset.incrementAndGet() <= requestsPerWindow) {
            return true;
        } else if (inReset.get()) { // only decrement if we exited reset mode, which means whileInRest has been set to zero.
            whileInReset.decrementAndGet();
            return false;
        }
        if (whileInReset.get() + requestsMade.incrementAndGet() <= requestsPerWindow) {
            return true;
        } else {
            requestsMade.decrementAndGet();
            return false;
        }
    }
}
