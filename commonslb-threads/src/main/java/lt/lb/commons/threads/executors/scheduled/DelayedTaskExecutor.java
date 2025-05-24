package lt.lb.commons.threads.executors.scheduled;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import lt.lb.commons.F;
import lt.lb.commons.Java;
import lt.lb.commons.containers.values.LongValue;
import lt.lb.commons.misc.numbers.Atomic;
import lt.lb.commons.threads.ExplicitFutureTask;
import lt.lb.commons.threads.SimpleThreadPool;
import lt.lb.commons.threads.ThreadPool;
import lt.lb.commons.threads.executors.BaseExecutor;
import lt.lb.commons.threads.executors.CloseableExecutor;
import lt.lb.commons.threads.sync.Awaiter;
import lt.lb.commons.threads.sync.Awaiter.AwaiterTime;
import lt.lb.commons.threads.sync.WaitTime;
import lt.lb.uncheckedutils.Checked;
import lt.lb.uncheckedutils.func.UncheckedRunnable;
import lt.lb.commons.threads.FailableRunnableFuture;

/**
 *
 * @author laim0nas100
 */
public class DelayedTaskExecutor extends BaseExecutor implements CloseableExecutor, ScheduledExecutorService {

    private class DTEFutureTask<T> extends ExplicitFutureTask<T> {

        public DTEFutureTask(Callable<T> callable) {
            super(callable);
        }

        public DTEFutureTask(Runnable runnable, T result) {
            super(runnable, result);
        }

        @Override
        protected void done() {
            Atomic.decrementAndGet(executing);
            super.done();
        }

    }

    protected ThreadPool pool;
    protected DelayQueue<DTEScheduledFuture> dq = new DelayQueue<>();
    protected AtomicInteger executing = new AtomicInteger(0);
    protected ExecutorService realExe;
    protected final int maxSchedulingThreads;
    protected final AtomicInteger schedulingThreadCount = new AtomicInteger(0);
    protected ReentrantLock lock = new ReentrantLock(true);
    protected Condition oneShotCondition = lock.newCondition();
    protected Condition fullCompletiontCondition = lock.newCondition();

    protected WaitTime pollTime = WaitTime.ofMillis(777);// fun number

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
        lock.lock();

        try {
            if (!lock.hasWaiters(oneShotCondition)) {
                return false;
            }
            for (DTEScheduledFuture future : dq) {
                if (future != null) {
                    if (future.isOneShot()) {
                        return false; // found unfinished oneshot, abort future completion
                    }
                }
            }
            oneShotCondition.signalAll();
        } finally {
            lock.unlock();
        }
        return true;
    }

    boolean cleanUp() {
        lock.lock();
        try {

            if (executing.get() > 0 || !dq.isEmpty()) {
                return false;
            }

            fullCompletiontCondition.signalAll();
        } finally {
            lock.unlock();
        }

        return false;
    }

    @Override
    protected <T> RunnableFuture<T> newTaskFor(Callable<T> task) {
        if (task instanceof DTEFutureTask) {
            return F.cast(task);
        } else {
            return new DTEFutureTask<>(task);
        }
    }

    @Override
    protected <T> RunnableFuture<T> newTaskFor(Runnable task, T res) {
        if (task instanceof DTEFutureTask) {
            return F.cast(task);
        } else {
            return new DTEFutureTask<>(task, res);
        }
    }

    @Override
    public void execute(Runnable command) {
        assertShutdown();
        Objects.requireNonNull(command);
        executeWithIncrement(realExe, command);
    }

    protected void executeSched(DTEScheduledFuture command) {
        assertShutdown();
        executeWithIncrement(command.taskExecutor, command);
    }

    protected void executeWithIncrement(Executor exe, Runnable command) {
        boolean maybeCompensate = true;
        Atomic.incrementAndGet(executing);
        FailableRunnableFuture ff;
        if (command instanceof DTEScheduledFuture) {
            ff = F.cast(command);
        } else {
            maybeCompensate = false;// setting exception invokes done anyway
            ff = F.cast(newTaskFor(command, null));// type check inside
        }
        try {
            exe.execute(ff);

        } catch (Throwable th) {
            if (maybeCompensate) {
                Atomic.decrementAndGet(executing);
            }
            ff.setException(th);

        }
    }

    protected void assertShutdown() {
        if (!open) {
            throw new IllegalArgumentException("Shutdown has been called, can't schedule more");
        }
    }

    protected <T extends DTEScheduledFuture<?>> T schedule(T future) {
        assertShutdown();
        return scheduleForSure(future);
    }

    protected <T extends DTEScheduledFuture<?>> T scheduleForSure(T future) {

        future.nanoScheduled.set(Java.getNanoTime());
        dq.add(future);

        maybestartSchedulingThread(false);
        return future;
    }

    protected void maybestartSchedulingThread(boolean onlyClean) {
        if (schedulingThreadCount.get() >= maxSchedulingThreads) {
            return; // fast exit
        }
        if (schedulingThreadCount.incrementAndGet() > maxSchedulingThreads) {
            schedulingThreadCount.decrementAndGet();
        } else {
            startSchedulingThread(onlyClean);
        }
    }

    protected void startSchedulingThread(final boolean onlyClean) {
        //we need to start thread

        Runnable handle = () -> {
            try {
                if (!onlyClean) {
                    while (!dq.isEmpty() || executing.get() != 0) {

                        if (!open) {
                            dq.clear();
                        } else {
                            DTEScheduledFuture take = dq.poll(pollTime.time, pollTime.unit);
                            if (open && take != null && !take.isDone()) {
                                executeSched(take);
                            }
                        }
                        cleanUp();
                        cleanUpOneShots();

                    }
                }

            } catch (Throwable th) {//could be interrupted, or task executor failed to execute

            } finally {

                try {
                    lock.lock();
                    // always signal
                    schedulingThreadCount.decrementAndGet();
                    cleanUpOneShots();
                    cleanUp();

                    if (onlyClean || !open) {//only clean up mode

                    } else if (!dq.isEmpty() || executing.get() != 0) {
                        startSchedulingThread(false);
                    }
                } finally {
                    lock.unlock();
                }
            }
        };
        pool.newThread(handle);

    }

    protected boolean fastExit() {
        return executing.get() == 0 && dq.isEmpty();
    }

    public AwaiterTime awaitOneShotCompletion() {
        return Awaiter.fromLockCondition(this::fastExit, lock, oneShotCondition);
    }

    public AwaiterTime awaitFullCompletion() {
        return Awaiter.fromLockCondition(this::fastExit, lock, fullCompletiontCondition);
    }

    @Override
    public void shutdown() {
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
    public List<Runnable> shutdownNow() {
        shutdown();
        return realExe.shutdownNow();
    }

    @Override
    public boolean isTerminated() {
        return realExe.isTerminated() && fastExit();
    }

    @Override
    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        return Awaiter.compositeTime(this::isTerminated, realExe::awaitTermination, awaitFullCompletion())
                .awaitBool(timeout, unit);
    }

    public DTEScheduledFuture schedule(Executor taskExe, WaitTime time, Runnable command) {
        assertShutdown();
        return schedule(new DTEScheduledFuture<>(this, taskExe, time, Executors.callable(command)));
    }

    public DTEScheduledFuture schedule(WaitTime time, Runnable command) {
        return schedule(realExe, time, command);
    }

    public DTEScheduledFuture schedule(Executor taskExe, WaitTime time, UncheckedRunnable command) {
        assertShutdown();
        return schedule(new DTEScheduledFuture<>(this, taskExe, time, Executors.callable(command)));
    }

    public DTEScheduledFuture schedule(WaitTime time, UncheckedRunnable command) {
        return schedule(realExe, time, command);
    }

    public <V> DTEScheduledFuture<V> schedule(Executor taskExe, WaitTime time, Callable<V> callable) {
        assertShutdown();
        return schedule(new DTEScheduledFuture<>(this, taskExe, time, callable));
    }

    public <V> DTEScheduledFuture<V> schedule(WaitTime time, Callable<V> callable) {
        return schedule(realExe, time, callable);
    }

    public ScheduledFuture<?> schedule(Executor taskExe, Runnable command, long delay, TimeUnit unit) {
        return schedule(taskExe, WaitTime.of(delay, unit), command);
    }

    @Override
    public ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit) {
        return schedule(realExe, WaitTime.of(delay, unit), command);
    }

    @Override
    public <V> ScheduledFuture<V> schedule(Callable<V> callable, long delay, TimeUnit unit) {
        return schedule(WaitTime.of(delay, unit), callable);
    }

    public <V> ScheduledFuture<V> schedule(Executor taskExe, Callable<V> callable, long delay, TimeUnit unit) {
        return schedule(taskExe, WaitTime.of(delay, unit), callable);
    }

    public DTELoopingLimitedScheduledFuture scheduleWithFixedDelayAndCondition(ScheduleLoopCondition condition, WaitTime time, Runnable command) {
        return scheduleWithFixedDelayAndCondition(realExe, condition, time, command);
    }

    public DTELoopingLimitedScheduledFuture scheduleWithFixedDelayAndCondition(Executor taskExe, ScheduleLoopCondition condition, WaitTime time, Runnable command) {
        return schedule(new DTELoopingLimitedScheduledFuture<>(condition, this, taskExe, time, Executors.callable(command)));
    }

    public DTELoopingLimitedScheduledFuture scheduleWithFixedDelay(Executor taskExe, WaitTime time, Runnable command) {
        return schedule(new DTELoopingLimitedScheduledFuture<>(ScheduleLoopCondition.always(true), this, taskExe, time, Executors.callable(command)));
    }

    public DTELoopingLimitedScheduledFuture scheduleWithFixedDelay(WaitTime time, Runnable command) {
        return scheduleWithFixedDelay(realExe, time, command);
    }

    protected void scheduleAtFixedRateContinue(
            Executor taskExe,
            PersistentForwardingScheduledFuture persFuture,
            final LongValue begunAtNanos,
            final LongValue times,
            final boolean keepExpectedPace,
            Runnable command,
            long initialDelay,
            long period,
            long resetAt
    ) {
        if (isShutdown()) {
            return;
        }
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

        if (times.incrementAndGet() >= resetAt) {//reset
            times.set(1L);
            if (keepExpectedPace) {// do some dept calculations
                begunAtNanos.set(now - initialDelay + diff);
            }
        }
        Callable<Void> call = () -> {
            //inside executor
            Throwable exception = Checked.checkedRun(command).orElse(null);
            if (exception != null) {
                persFuture.setException(exception);
                return null;
            }
//            command.run();
            //self cancel on throw
            if (persFuture.isCancelled()) {
                return null;
            }

            scheduleAtFixedRateContinue(taskExe, persFuture, begunAtNanos, times, keepExpectedPace, command, initialDelay, period, resetAt);
            return null;
        };
        DTEScheduledFuture dteScheduledFuture = new DTEScheduledFuture(false, this, taskExe, WaitTime.ofNanos(p), call);// looping, so not a oneshot
        persFuture.set(scheduleForSure(dteScheduledFuture));
    }

    @Override
    public ScheduledFuture<?> scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit) {
        return scheduleAtFixedRate(realExe, command, initialDelay, period, unit);
    }

    public ScheduledFuture<?> scheduleAtFixedRate(Executor taskExe, Runnable command, long initialDelay, long period, TimeUnit unit) {
        assertShutdown();
        if (initialDelay <= 0) {
            throw new IllegalArgumentException("initial delay must be posivite");
        }
        if (period <= 0) {
            throw new IllegalArgumentException("period must be positive");
        }
        Objects.requireNonNull(taskExe, "Task executor is not provided");

        long delayNano = WaitTime.of(initialDelay, unit).toNanosAssert();
        long periodNano = WaitTime.of(period, unit).toNanosAssert();

        //reset at least once in a while, if been running for that long
        long resetAt = Long.MAX_VALUE / periodNano;
        PersistentForwardingScheduledFuture persFuture = new PersistentForwardingScheduledFuture();
        scheduleAtFixedRateContinue(
                taskExe,
                persFuture,
                new LongValue(Java.getNanoTime()),
                new LongValue(0),
                true,
                command,
                delayNano,
                periodNano,
                resetAt
        );
        return persFuture;
    }

    @Override
    public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command, long initialDelay, long delay, TimeUnit unit) {
        return scheduleWithFixedDelay(realExe, command, initialDelay, delay, unit);
    }

    public ScheduledFuture<?> scheduleWithFixedDelay(Executor taskExe, Runnable command, long initialDelay, long delay, TimeUnit unit) {
        assertShutdown();
        if (initialDelay <= 0) {
            throw new IllegalArgumentException("initial delay must be posivite");
        }
        if (delay <= 0) {
            throw new IllegalArgumentException("delay must be positive");
        }
        Objects.requireNonNull(taskExe, "Task executor is not provided");
        long delayNano = WaitTime.of(initialDelay, unit).toNanosAssert();
        long periodNano = WaitTime.of(delay, unit).toNanosAssert();
        PersistentForwardingScheduledFuture persFuture = new PersistentForwardingScheduledFuture();
        scheduleAtFixedRateContinue(
                taskExe,
                persFuture,
                new LongValue(Long.MIN_VALUE),
                new LongValue(0),
                false,
                command,
                delayNano,
                periodNano,
                Long.MAX_VALUE);
        return persFuture;
    }

    @Override
    public boolean isShutdown() {
        return !open;
    }

    @Override
    public int parallelism() {
        return maxSchedulingThreads;
    }
}
