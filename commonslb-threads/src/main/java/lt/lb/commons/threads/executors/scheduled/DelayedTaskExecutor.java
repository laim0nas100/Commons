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
import lt.lb.commons.threads.ThreadPool;
import lt.lb.commons.threads.executors.BaseExecutor;
import lt.lb.commons.threads.executors.CloseableExecutor;
import lt.lb.commons.threads.sync.Awaiter;
import lt.lb.commons.threads.sync.Awaiter.AwaiterTime;
import lt.lb.commons.threads.sync.WaitTime;
import com.github.laim0nas100.uncheckedutils.Checked;
import com.github.laim0nas100.uncheckedutils.func.UncheckedRunnable;
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
            Atomic.decrementAndGet(executingOneShots);
            super.done();
        }

    }

    protected ThreadPool pool;
    protected DelayQueue<DTEScheduledFuture> dq = new DelayQueue<>();
    protected AtomicInteger executing = new AtomicInteger(0);
    protected AtomicInteger executingOneShots = new AtomicInteger(0);
    protected ExecutorService realExe;
    protected final int maxSchedulingThreads;
    protected final AtomicInteger schedulingThreadCount = new AtomicInteger(0);
    protected ReentrantLock lock = new ReentrantLock(true);
    protected Condition oneShotCond = lock.newCondition();
    protected Condition fullCompletionCond = lock.newCondition();
    /**
     * initial number, but can grow. Doesn't matter really, just a time until
     * scheduling thread awaits new tasks to pass to main executor or signal any
     * awaiters. To not spin wait, we back-off exponentially up to
     * {@link DelayedTaskExecutor#MAX_POLL_TIME}. More scheduling threads will
     * more often signal any awaiters for one shot completion.
     */
    public static final long INITIAL_POLL_TIME = WaitTime.ofMillis(10).toNanos();
    public static final long MAX_POLL_TIME = WaitTime.ofSeconds(8).toNanos();

    public DelayedTaskExecutor() {
        this(1, Checked.createDefaultExecutorService());
    }

    public DelayedTaskExecutor(int maxSchedulingThreads) {
        this(assertScheduling(maxSchedulingThreads), Checked.createDefaultExecutorService());
    }

    public DelayedTaskExecutor(ExecutorService reaExecutor) {
        this(1, reaExecutor);
    }

    public DelayedTaskExecutor(int maxSchedulingThreads, ExecutorService realExe) {
        this(maxSchedulingThreads, realExe, createDefaultThreadPool(DelayedTaskExecutor.class));
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
        pool.setThreadsStarting(true);
    }

    boolean cleanOneShotSignal() {
        lock.lock();

        try {
            if (!lock.hasWaiters(oneShotCond)) {
                return false;
            }
            if (executingOneShots.get() > 0) {
                return false;
            }
            for (DTEScheduledFuture future : dq) {
                if (future != null) {
                    if (future.isOneShot()) {
                        return false; // found unsubmitted oneshot, abort future completion
                    }
                }
            }
            oneShotCond.signalAll();
        } finally {
            lock.unlock();
        }
        return true;
    }

    boolean cleanSignal() {
        lock.lock();
        try {

            if (!lock.hasWaiters(fullCompletionCond)) {
                return false;
            }
            if (hasExecuting() || !dq.isEmpty()) {
                return false;
            }

            fullCompletionCond.signalAll();
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

    protected void executeFromSched(DTEScheduledFuture command) {
        assertShutdown();
        executeWithIncrement(command.taskExecutor, command);
    }

    protected void executeWithIncrement(Executor exe, Runnable command) {

        boolean decrementOnRejection = false;
        boolean oneShot = false;
        FailableRunnableFuture ff;
        if (command instanceof DTEScheduledFuture) {
            DTEScheduledFuture dte = F.cast(command);
            ff = dte;
            // book-keeping is done inside the run method, so rejected tasks do not decrement
            decrementOnRejection = true;
            oneShot = dte.isOneShot();
            Atomic.incrementAndGet(oneShot ? executingOneShots : executing);
        } else {

            //decremented by done method regardless if it was rejected.
            ff = F.cast(newTaskFor(command, null));// type check inside
            Atomic.incrementAndGet(executingOneShots);
        }
        try {

            exe.execute(ff);

        } catch (Throwable th) {
            //rejected or internal executor error
            ff.setException(th);
            if (decrementOnRejection) {
                Atomic.decrementAndGet(oneShot ? executingOneShots : executing);
            }

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

        maybeStartSchedulingThread(false);
        return future;
    }

    protected boolean hasExecuting() {
        return executingOneShots.get() > 0 || executing.get() > 0;
    }

    protected int executingTotal() {
        return executing.get() + executingOneShots.get();
    }

    protected void maybeStartSchedulingThread(boolean onlyClean) {
        if (schedulingThreadCount.get() >= maxSchedulingThreads) {
            return; // fast exit
        }
        if (Atomic.incrementAndGet(schedulingThreadCount) > maxSchedulingThreads) {
            Atomic.decrementAndGet(schedulingThreadCount);
        } else {
            startSchedulingThread(onlyClean);
        }
    }

    protected void schedulingLoop(boolean onlyClean) {
        try {
            if (!onlyClean) {
                long nanosTimeout = INITIAL_POLL_TIME;
                while (!dq.isEmpty() || hasExecuting()) {

                    if (!open) {
                        dq.clear();
                    } else {
                        DTEScheduledFuture take = dq.poll(nanosTimeout, TimeUnit.NANOSECONDS);
                        if (open && take != null && !take.isDone()) {
                            executeFromSched(take);
                            //reset poll time
                            nanosTimeout = INITIAL_POLL_TIME;
                        } else if (take == null && nanosTimeout != MAX_POLL_TIME) { // increase poll time
                            nanosTimeout = Math.min(nanosTimeout * 2, MAX_POLL_TIME);
                        }
                    }
                    cleanOneShotSignal();
                    cleanSignal();

                }
            }

        } catch (Throwable ignore) {//could be interrupted or shutdown already after awaiting, so just exit in case the thing is closed

        } finally {

            try {
                lock.lock();
                // always signal
                Atomic.decrementAndGet(schedulingThreadCount);
                cleanOneShotSignal();
                cleanSignal();

                if (onlyClean || !open) {//only clean signal mode or time to exit

                } else if (!dq.isEmpty() || executingTotal() != 0) {
                    maybeStartSchedulingThread(false); // maybe restart itselt
                }
            } finally {
                lock.unlock();
            }
        }
    }

    protected void startSchedulingThread(final boolean onlyClean) {
        //we need to start thread
        pool.newThread(() -> schedulingLoop(onlyClean));
    }

    protected boolean fastExit() {
        return !hasExecuting() && dq.isEmpty();
    }

    public AwaiterTime awaitOneShotCompletion() {
        return Awaiter.fromLockCondition(this::fastExit, lock, oneShotCond);
    }

    public AwaiterTime awaitFullCompletion() {
        return Awaiter.fromLockCondition(this::fastExit, lock, fullCompletionCond);
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
        cleanSignal();
        cleanOneShotSignal();
    }

    /**
     * Shuts down the passed real executor also, doesn't return the scheduled
     * tasks, only the ones passed to the real executor. {@inheritDoc }
     *
     * @return
     */
    @Override
    public List<Runnable> shutdownNow() {
        shutdown();
        return realExe.shutdownNow();
    }

    @Override
    public boolean isTerminated() {
        return realExe.isTerminated() && fastExit();
    }

    /**
     * Real executor must also be terminated sometime in the future or this
     * method will always timeout. If real executor gets terminated before all
     * scheduled tasks are submitted, then these never will be executed and will
     * terminate as cancelled because the real executor rejection.
     * {@inheritDoc }
     *
     */
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
