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
    protected final AtomicLong lastReset;
    protected final AtomicBoolean inReset = new AtomicBoolean(false);
    protected final AtomicInteger whileInReset = new AtomicInteger();

    public RequestThrottle(WaitTime timeWindow, int requestsPerWindow) {
        this.timeWindow = Objects.requireNonNull(timeWindow);
        this.requestsPerWindow = Math.max(1, requestsPerWindow);
        this.lastReset = new AtomicLong(timeIncement());
        waitNanos = timeWindow.convert(TimeUnit.NANOSECONDS).time;
    }

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
                requestsMade.addAndGet(Math.abs(whileInReset.getAndSet(0)));
            }
        }
    }

    /**
     * Using both signs to determine if increment was successful. Will return
     * negative value of the same amount if increment was not successful.
     *
     * @param atomic
     * @param toAdd
     * @param limit
     * @param sumOffset
     * @return
     */
    public static int signedAccumulate(AtomicInteger atomic, final int toAdd, final int limit, final int sumOffset) {
        return atomic.accumulateAndGet(toAdd, (current, add) -> {
            int abs = Math.abs(current);
            int sum = abs + add;
            return (sumOffset + sum) <= limit ? sum : -abs;
        });
    }

    public boolean request() {
        updateTime(timeIncement());
        if (inReset.get()) {
            return signedAccumulate(whileInReset, 1, requestsPerWindow, 0) >= 0;
        } else {
            return signedAccumulate(requestsMade, 1, requestsPerWindow, Math.abs(whileInReset.get())) >= 0;
        }
    }

    /**
     * Should be always incrementing
     * @return 
     */
    protected long timeIncement() {
        return Java.getNanoTime();
    }
}
