package lt.lb.commons.threads.executors.scheduled;

import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import lt.lb.commons.Java;
import lt.lb.commons.threads.executors.CloseableExecutor;
import lt.lb.commons.threads.sync.Awaiter;
import lt.lb.commons.threads.sync.WaitTime;
import lt.lb.commons.func.unchecked.UncheckedRunnable;

/**
 *
 * @author laim0nas100
 */
public class DelayedTaskExecutor implements CloseableExecutor {

    protected ThreadGroup tg = new ThreadGroup("DelayedTaskExecutor");
    protected DelayQueue<DTEScheduledFuture> dq = new DelayQueue<>();
    protected Executor realExe;
    protected volatile boolean shutdown;
    protected final int maxSchedulingThreads;
    protected final AtomicInteger threadCount = new AtomicInteger(0);
    protected AtomicReference<CompletableFuture> oneShotCompletion = new AtomicReference<>();

    public DelayedTaskExecutor() {
        this(ForkJoinPool.commonPool());
    }

    public DelayedTaskExecutor(Executor reaExecutor) {
        this(reaExecutor, 1);
    }

    public DelayedTaskExecutor(Executor realExe, int maxSchedulingThreads) {
        this.realExe = Objects.requireNonNull(realExe);
        if (maxSchedulingThreads < 0) {
            throw new IllegalArgumentException("Max scheduling threads should be at least 1");
        }
        this.maxSchedulingThreads = maxSchedulingThreads;
    }

    void cleanUp() {
        CompletableFuture oneShots = oneShotCompletion.get();
        if (oneShots != null) {
            for (DTEScheduledFuture future : dq) {
                if (future != null) {
                    if (future.oneShot) {
                        return; // found unfinished oneshot
                    }
                }
            }
            oneShots.complete(null);
            oneShotCompletion.compareAndSet(oneShots, new CompletableFuture());
        }
    }

    @Override
    public void execute(Runnable command) {
        realExe.execute(command);
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

    <T extends DTEScheduledFuture<?>> T schedule(T future) {

        if (shutdown) {
            throw new IllegalArgumentException("Shutdown has been called, can't schedule more");
        }
        future.nanoScheduled.set(Java.getNanoTime());
        dq.add(future);

        if (threadCount.incrementAndGet() > maxSchedulingThreads) {
            threadCount.decrementAndGet();
        } else {
            startSchedulingThread();
        }
        return future;

    }

    protected void startSchedulingThread() {
        //we need to start thread

        Runnable handle = () -> {
            try {
                while (!dq.isEmpty() && !shutdown) {

                    DTEScheduledFuture take = dq.take();
                    if (take != null) {
                        execute(take);
                    }
                    cleanUp();

                }

            } catch (Throwable th) {
                threadCount.decrementAndGet();
                if (!dq.isEmpty() && !shutdown) {
                    startSchedulingThread();
                }
                cleanUp();
            }
        };
        new Thread(tg, handle).start();

    }

    public DTEScheduledFuture schedule(WaitTime time, Runnable command) {
        return schedule(new DTEScheduledFuture<>(this, time, Executors.callable(command)));
    }

    public DTEScheduledFuture schedule(WaitTime time, UncheckedRunnable command) {
        return schedule(new DTEScheduledFuture<>(this, time, Executors.callable(command)));
    }
    
    public <V> DTEScheduledFuture<V> schedule(WaitTime time, Callable<V> callable) {
        return schedule(new DTEScheduledFuture<>(this, time, callable));
    }

    public Awaiter awaitOneShotCompletion() {
        Awaiter awaiter = Awaiter.fromFutureAtomicReference(oneShotCompletion, CompletableFuture::new);
        cleanUp();
        return awaiter;
    }

    @Override
    public void close() {
        shutdown = true;
        tg.interrupt();
    }

}
