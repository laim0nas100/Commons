package lt.lb.commons.threads.service;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import lt.lb.commons.threads.executors.FastWaitingExecutor;
import lt.lb.commons.threads.executors.scheduled.DelayedTaskExecutor;
import lt.lb.commons.threads.sync.WaitTime;

/**
 * {@link ServiceExecutorAggregatorBase}, but using non-standard implementation
 * executors, that does not keep active threads.
 *
 * @author laim0nas100
 */
public class ServiceExecutorAggregatorLazy extends ServiceExecutorAggregatorBase {

    protected WaitTime defaultWaitTime = WaitTime.ofSeconds(5);

    public ServiceExecutorAggregatorLazy() {
    }

    @Override
    protected ScheduledExecutorService createScheduledExecutor(int threads) {
        return new DelayedTaskExecutor(createExecutor(threads), threads);
    }

    @Override
    protected ExecutorService createExecutor(int threads) {
        return new FastWaitingExecutor(threads, defaultWaitTime);
    }

}
