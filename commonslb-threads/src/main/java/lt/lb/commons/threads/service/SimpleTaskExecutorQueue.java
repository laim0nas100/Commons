package lt.lb.commons.threads.service;

import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;

/**
 *
 * @author laim0nas100
 */
public class SimpleTaskExecutorQueue extends BasicTaskExecutorQueue {

    protected ExecutorService executor;
    protected ScheduledExecutorService scheduler;

    public SimpleTaskExecutorQueue(ScheduledExecutorService scheduler, ExecutorService executor) {
        this.scheduler = Objects.requireNonNull(scheduler, "ScheduledExecutorService must not be null");
        this.executor = Objects.requireNonNull(executor, "ExecutorService must not be null");
    }

    public SimpleTaskExecutorQueue(ScheduledExecutorService scheduler) {
        this(scheduler, scheduler);
    }

    @Override
    public ScheduledExecutorService getScheduler() {
        return scheduler;
    }

    @Override
    public ExecutorService getExecutor() {
        return executor;
    }

}
