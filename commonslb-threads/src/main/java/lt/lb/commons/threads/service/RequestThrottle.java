package lt.lb.commons.threads.service;

import java.util.Objects;
import java.util.concurrent.TimeUnit;
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
    protected final AtomicLong lastCall = new AtomicLong(Java.getNanoTime());

    public RequestThrottle(WaitTime timeWindow, int requestsPerWindow) {
        this.timeWindow = Objects.requireNonNull(timeWindow);
        this.requestsPerWindow = Math.max(1, requestsPerWindow);
        waitNanos = timeWindow.convert(TimeUnit.NANOSECONDS).time;
    }

    protected void updateTime(long now) {
        long call = lastCall.get();

        long diff = now - call;

        if (diff >= waitNanos) {
            lastCall.set(now);
            int unchaged = requestsMade.get();
            while (true) {
                int current = requestsMade.get();
                if (current != unchaged) {
                    return;
                }

                if (requestsMade.compareAndSet(current, 0)) {
                    return;
                }
            }
        }
    }

    public boolean request() {
        updateTime(Java.getNanoTime());
        if (requestsMade.incrementAndGet() <= requestsPerWindow) {
            return true;
        } else {
            requestsMade.decrementAndGet();
            return false;
        }
    }
}
