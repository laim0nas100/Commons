package lt.lb.commons.threads.service;

import java.util.Objects;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicInteger;
import lt.lb.commons.threads.sync.WaitTime;

/**
 * Only allow limited number of requests per given time window
 *
 * @author laim0nas100
 */
public class ServiceRequestThrottle {

    protected final ScheduledExecutorService service;
    protected final WaitTime timeWindow;
    protected final Runnable defaultRequest;
    protected final AtomicInteger requestsMade = new AtomicInteger(0);
    protected final int requestsPerWindow;

    public ServiceRequestThrottle(ScheduledExecutorService service, WaitTime timeWindow, int requestsPerWindow, Runnable defaultRequest) {
        this.service = Objects.requireNonNull(service);
        this.timeWindow = Objects.requireNonNull(timeWindow);
        this.requestsPerWindow = Math.max(1, requestsPerWindow);
        this.defaultRequest = Objects.requireNonNull(defaultRequest);

        service.scheduleWithFixedDelay(() -> {
            while(true){
                if(requestsMade.compareAndSet(requestsMade.get(), 0)){ // nothing must be lost, so try until ok
                    return;
                }
            }
        }, timeWindow.time, timeWindow.time, timeWindow.unit);
    }

    public void addRequest() {
        addRequest(defaultRequest);
    }

    public void addRequest(Runnable run) {
        Objects.requireNonNull(run);
        if (requestsMade.incrementAndGet() <= requestsPerWindow) {
            run.run();
        } else {
            requestsMade.decrementAndGet();
        }
    }

}

