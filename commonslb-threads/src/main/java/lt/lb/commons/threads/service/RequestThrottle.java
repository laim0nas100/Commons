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
        this.timeWindow = Objects.requireNonNull(timeWindow);
        this.requestsPerWindow = Math.max(1, requestsPerWindow);
        this.lastReset = new AtomicLong(timeIncement());
        waitNanos = timeWindow.convert(TimeUnit.NANOSECONDS).time;
    }

    protected void updateTime(long now) {
        long call = lastReset.get();
        if (now - call >= waitNanos) {
            if (lastReset.compareAndSet(call, now)) {
                requestsMade.set(0);
            }
        }
    }

    public boolean request() {

        updateTime(timeIncement());
        return Atomic.signedIncrement(requestsMade, requestsPerWindow) >= 0;
    }

    /**
     * Should be always incrementing
     *
     * @return
     */
    protected long timeIncement() {
        return Java.getNanoTime();
    }
}
