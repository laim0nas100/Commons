package com.github.laim0nas100.commonslb.threads.service;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import com.github.laim0nas100.commonslb.threads.executors.FastWaitingExecutor;
import com.github.laim0nas100.commonslb.threads.executors.scheduled.DelayedTaskExecutor;
import com.github.laim0nas100.commonslb.threads.sync.WaitTime;

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
        return new DelayedTaskExecutor(1,createExecutor(threads));
    }

    @Override
    protected ExecutorService createExecutor(int threads) {
        return new FastWaitingExecutor(threads, defaultWaitTime);
    }

}
