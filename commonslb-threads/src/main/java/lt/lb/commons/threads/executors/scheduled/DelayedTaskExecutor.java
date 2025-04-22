package lt.lb.commons.threads.executors.scheduled;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import lt.lb.commons.Java;
import lt.lb.commons.containers.values.LongValue;
import lt.lb.commons.threads.SimpleThreadPool;
import lt.lb.commons.threads.ThreadPool;
import lt.lb.commons.threads.executors.CloseableExecutor;
import lt.lb.commons.threads.sync.Awaiter.AwaiterTime;
import lt.lb.commons.threads.sync.SimpleAwaiter;
import lt.lb.commons.threads.sync.WaitTime;
import lt.lb.uncheckedutils.Checked;
import lt.lb.uncheckedutils.func.UncheckedRunnable;

/**
 *
 * @author laim0nas100
 */
public class DelayedTaskExecutor extends AbstractExecutorService implements CloseableExecutor, ScheduledExecutorService {

    protected ThreadPool pool;
    protected DelayQueue<DTEScheduledFuture> dq = new DelayQueue<>();
    protected ConcurrentLinkedDeque<Future> executed = new ConcurrentLinkedDeque<>();
    protected ExecutorService realExe;
    protected volatile boolean open = true;
    protected final int maxSchedulingThreads;
    protected final AtomicInteger schedulingThreadCount = new AtomicInteger(0);
    protected SimpleAwaiter oneShotCompletion = new SimpleAwaiter();
    protected SimpleAwaiter fullCompletion = new SimpleAwaiter();

    protected int maxPollTimeSeconds = 60;

    public DelayedTaskExecutor() {
        this(Checked.createDefaultExecutorService());
    }

    public DelayedTaskExecutor(int maxSchedulingThreads) {
        this(assertScheduling(maxSchedulingThreads), Checked.createDefaultExecutorService());
    }

    public DelayedTaskExecutor(ExecutorService reaExecutor) {
        this(1, reaExecutor);
    }

    public DelayedTaskExecutor(int maxSchedulingThreads, ExecutorService realExe) {
        this(maxSchedulingThreads, realExe, new SimpleThreadPool(DelayedTaskExecutor.class));
    }

    private static int assertScheduling(int threads) {
        if (threads <= 0) {
            throw new IllegalArgumentException("Max scheduling threads should be at least 1");
        }
        return threads;
    }

    public DelayedTaskExecutor(int maxSchedulingThreads, ExecutorService realExe, ThreadPool pool) {
        this.realExe = Objects.requireNonNull(realExe);
        this.maxSchedulingThreads = assertScheduling(maxSchedulingThreads);
        this.pool = Objects.requireNonNull(pool);
        pool.setStarting(true);
    }

    boolean cleanUpOneShots() {
        if (!oneShotCompletion.hasWaiters()) {
            return false;
        }

        for (DTEScheduledFuture future : dq) {
            if (future != null) {
                if (future.isOneShot()) {
                    return false; // found unfinished oneshot, abort future completion
                }
            }
        }
        oneShotCompletion.completeAndReset();
        return true;
    }

    boolean cleanUp() {

        Iterator<Future> iterator = executed.iterator();
        int size = executed.size();
        boolean complete = true;
        while (--size >= 0) {
            Future fut = iterator.next();
            if (fut == null) {
                continue;
            }
            if (fut.isDone()) {
                iterator.remove();
                iterator = executed.iterator();
            } else {
                complete = false;
            }

        }
        if (!complete) {
            return false;
        }

        for (DTEScheduledFuture future : dq) {
            if (future != null) {
                return false;
            }
        }

        fullCompletion.completeAndReset();
        return false;
    }

    @Override
    public void execute(Runnable command) {
        assertShutdown();
        Objects.requireNonNull(command);
        realExe.submit(command);
    }

    public void executeSched(Runnable command) {
        assertShutdown();
        Objects.requireNonNull(command);
        executed.add(realExe.submit(command));
    }

    protected void assertShutdown() {
        if (!open) {
            throw new IllegalArgumentException("Shutdown has been called, can't schedule more");
        }
    }

    protected <T extends DTEScheduledFuture<?>> T schedule(T future) {
        assertShutdown();

        future.nanoScheduled.set(Java.getNanoTime());
        dq.add(future);

        maybestartSchedulingThread(false);
        return future;

    }

    protected void maybestartSchedulingThread(boolean onlyClean) {
        if (schedulingThreadCount.incrementAndGet() > maxSchedulingThreads) {
            schedulingThreadCount.decrementAndGet();
        } else {
            startSchedulingThread(onlyClean);
        }
    }

    protected void startSchedulingThread(boolean onlyClean) {
        //we need to start thread

        Runnable handle = () -> {
            try {
                if (!onlyClean) {
                    while (!dq.isEmpty() || !executed.isEmpty()) {

                        if (!open) {
                            dq.clear();
                        } else if (!dq.isEmpty()) {
                            DTEScheduledFuture take = dq.poll(maxPollTimeSeconds, TimeUnit.SECONDS);
                            if (open && take != null && !take.isCancelled()) {
                                executeSched(take);
                            }
                        }
                        cleanUp();
                        cleanUpOneShots();

                    }
                }

            } catch (Throwable th) {//could be interrupted

            } finally {
                schedulingThreadCount.decrementAndGet();
                if (!open || onlyClean) {//only clean up mode
                    cleanUpOneShots();
                    cleanUp();
                } else if (!dq.isEmpty() || !executed.isEmpty()) {
                    startSchedulingThread(false);
                }
            }
        };
        pool.newThread(handle);

    }

    public AwaiterTime awaitOneShotCompletion() {

//        Awaiter awaiter = Awaiter.fromFutureAtomicReference(oneShotCompletion, CompletableFuture::new);
        oneShotCompletion.prep();
        cleanUpOneShots();
        return oneShotCompletion.singleUse();
    }

    public AwaiterTime awaitFullCompletion() {
//        Awaiter awaiter = Awaiter.fromFutureAtomicReference(fullCompletion, CompletableFuture::new);
        fullCompletion.prep();
        cleanUp();
        return fullCompletion.singleUse();
    }

    @Override
    public void close() {
        if (!open) {
            return;
        }
        open = false;

        dq.clear();
        pool.interruptWaiting();
        realExe.shutdown();
        cleanUp();
        cleanUpOneShots();
    }

    @Override
    public void shutdown() {
        close();
    }

    @Override
    public List<Runnable> shutdownNow() {
        shutdown();
        return realExe.shutdownNow();
    }

    @Override
    public boolean isTerminated() {
        return realExe.isTerminated();
    }

    @Override
    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        return realExe.awaitTermination(timeout, unit);
    }

    public DTEScheduledFuture schedule(WaitTime time, Runnable command) {
        assertShutdown();
        return schedule(new DTEScheduledFuture<>(this, time, Executors.callable(command)));
    }

    public DTEScheduledFuture schedule(WaitTime time, UncheckedRunnable command) {
        assertShutdown();
        return schedule(new DTEScheduledFuture<>(this, time, Executors.callable(command)));
    }

    public <V> DTEScheduledFuture<V> schedule(WaitTime time, Callable<V> callable) {
        assertShutdown();
        return schedule(new DTEScheduledFuture<>(this, time, callable));
    }

    @Override
    public ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit) {
        assertShutdown();
        return schedule(WaitTime.of(delay, unit), command);
    }

    @Override
    public <V> ScheduledFuture<V> schedule(Callable<V> callable, long delay, TimeUnit unit) {
        assertShutdown();
        return schedule(WaitTime.of(delay, unit), callable);
    }

    public DTELoopingLimitedScheduledFuture scheduleWithFixedDelayAndCondition(ScheduleLoopCondition condition, WaitTime time, UncheckedRunnable command) {
        return schedule(new DTELoopingLimitedScheduledFuture<>(condition, this, time, Executors.callable(command)));
    }

    public DTELoopingLimitedScheduledFuture scheduleWithFixedDelayAndCondition(ScheduleLoopCondition condition, WaitTime time, Runnable command) {
        return schedule(new DTELoopingLimitedScheduledFuture<>(condition, this, time, Executors.callable(command)));
    }

    public DTELoopingScheduledFuture scheduleWithFixedDelay(WaitTime time, Runnable command) {
        return schedule(new DTELoopingScheduledFuture<>(this, time, Executors.callable(command)));
    }

    public DTELoopingScheduledFuture scheduleWithFixedDelay(WaitTime time, UncheckedRunnable command) {
        return schedule(new DTELoopingScheduledFuture<>(this, time, Executors.callable(command)));
    }

    protected void scheduleAtFixedRateContinue(
            PersistentForwardingScheduledFuture persFuture,
            final LongValue begunAtNanos,
            final LongValue times,
            boolean keepExpectedPace,
            Runnable command,
            long initialDelay,
            long period,
            long resetTimes
    ) {
        long p = period;
        long diff = 0;
        long now = 0;
        if (times.get() == 0L) {//first
            p = initialDelay;
        } else if (keepExpectedPace) {
            long expectedPace = begunAtNanos.get() + initialDelay + (times.get() * period);
            now = Java.getNanoTime();
            diff = expectedPace - now;
            p = Math.max(0, diff);
        }

        if (times.incrementAndGet() >= resetTimes) {//reset
            times.set(1L);
            if (keepExpectedPace) {// do some dept calculations
                begunAtNanos.set(now - initialDelay + diff);
            }
        }
        persFuture.set(schedule(WaitTime.ofNanos(p), () -> {
            if (persFuture.isCancelled()) {
                return null;
            }
            Checked.checkedRun(command);// continued run
            if (persFuture.isCancelled()) {
                return null;
            }

            scheduleAtFixedRateContinue(persFuture, begunAtNanos, times, keepExpectedPace, command, initialDelay, period, resetTimes);
            return null;
        }));
    }

    @Override
    public ScheduledFuture<?> scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit) {
        assertShutdown();
        if (initialDelay < 0) {
            throw new IllegalArgumentException("Negative initial delay");
        }
        if (period < 0) {
            throw new IllegalArgumentException("Negative period");
        }

        long delayNano = TimeUnit.NANOSECONDS.convert(initialDelay, unit);
        long periodNano = TimeUnit.NANOSECONDS.convert(period, unit);

        //reset at least once in a while, if been running for that long
        long resetAt = (WaitTime.ofDays(400).convert(TimeUnit.NANOSECONDS).time - delayNano) / periodNano;
        PersistentForwardingScheduledFuture persFuture = new PersistentForwardingScheduledFuture();
        scheduleAtFixedRateContinue(persFuture, new LongValue(Java.getNanoTime()), new LongValue(0), true, command, delayNano, periodNano, resetAt);
        return persFuture;
    }

    @Override
    public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command, long initialDelay, long delay, TimeUnit unit) {
        assertShutdown();
        if (initialDelay < 0) {
            throw new IllegalArgumentException("Negative initial delay");
        }
        if (delay < 0) {
            throw new IllegalArgumentException("Negative delay");
        }
        long delayNano = TimeUnit.NANOSECONDS.convert(initialDelay, unit);
        long periodNano = TimeUnit.NANOSECONDS.convert(delay, unit);
        PersistentForwardingScheduledFuture persFuture = new PersistentForwardingScheduledFuture();
        scheduleAtFixedRateContinue(persFuture, new LongValue(Long.MIN_VALUE), new LongValue(0), false, command, delayNano, periodNano, Long.MAX_VALUE);
        return persFuture;
    }

    @Override
    public boolean isShutdown() {
        return !open;
    }
}
