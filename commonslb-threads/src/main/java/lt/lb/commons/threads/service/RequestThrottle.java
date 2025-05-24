package lt.lb.commons.threads.service;

import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import lt.lb.commons.Java;
import lt.lb.commons.misc.numbers.Atomic;
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
    protected final AtomicLong lastReset;

    public RequestThrottle(WaitTime timeWindow, int requestsPerWindow) {
        if (requestsPerWindow < 1) {
            throw new IllegalArgumentException("requests per window must be positive:" + requestsPerWindow);
        }
        this.timeWindow = Objects.requireNonNull(timeWindow);
        this.requestsPerWindow = requestsPerWindow;
        this.lastReset = new AtomicLong(timeIncrement());
        waitNanos = timeWindow.toNanosAssert();
    }

    protected void updateTime(long now) {
        long call = lastReset.get();
        if (now - call >= waitNanos) {
            if (lastReset.compareAndSet(call, now)) {// reset only once
                requestsMade.set(0);
            }
        }
    }

    public boolean request() {

        updateTime(timeIncrement());
        return Atomic.signedIncrement(requestsMade, requestsPerWindow) >= 0;
    }

    /**
     * Should be always incrementing
     *
     * @return
     */
    protected long timeIncrement() {
        return Java.getNanoTime();
    }
}
