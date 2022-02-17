package lt.lb.commons.threads.service;

import java.util.Objects;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicInteger;
import lt.lb.commons.misc.numbers.Atomic;
import lt.lb.commons.threads.sync.WaitTime;

/**
 * Only allow limited number of requests per given time window
 *
 * @author laim0nas100
 */
public class ServiceRequestThrottle {

    protected final WaitTime timeWindow;
    protected final AtomicInteger requestsMade = new AtomicInteger(0);
    protected final int requestsPerWindow;
    protected final ScheduledFuture<?> scheduledFuture;

    public ServiceRequestThrottle(ScheduledExecutorService service, WaitTime timeWindow, int requestsPerWindow) {
        this.timeWindow = Objects.requireNonNull(timeWindow);
        this.requestsPerWindow = Math.max(1, requestsPerWindow);

        scheduledFuture = Objects.requireNonNull(service).scheduleWithFixedDelay(() -> {
            requestsMade.set(0);
        }, timeWindow.time, timeWindow.time, timeWindow.unit);
    }


    public boolean request() {
        return Atomic.signedIncrement(requestsMade, requestsPerWindow) >= 0;
    }

    public void dispose() {
        scheduledFuture.cancel(false);
    }

}
