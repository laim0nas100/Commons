package lt.lb.commons.threads.executors.scheduled;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import lt.lb.commons.Java;
import lt.lb.commons.threads.ForwardingScheduledFuture;
import lt.lb.commons.threads.executors.CloseableExecutor;
import lt.lb.commons.threads.executors.FastWaitingExecutor;
import lt.lb.commons.threads.sync.Awaiter;
import lt.lb.commons.threads.sync.WaitTime;
import lt.lb.uncheckedutils.func.UncheckedRunnable;

/**
 *
 * @author laim0nas100
 */
public class DelayedTaskExecutor extends AbstractExecutorService implements CloseableExecutor, ScheduledExecutorService {

    protected ThreadGroup tg = new ThreadGroup("DelayedTaskExecutor");
    protected DelayQueue<DTEScheduledFuture> dq = new DelayQueue<>();
    protected ConcurrentLinkedDeque<Future> executed = new ConcurrentLinkedDeque<>();
    protected ExecutorService realExe;
    protected volatile boolean shutdown;
    protected final int maxSchedulingThreads;
    protected final AtomicInteger threadCount = new AtomicInteger(0);
    protected AtomicReference<CompletableFuture> oneShotCompletion = new AtomicReference<>();
    protected AtomicReference<CompletableFuture> fullCompletion = new AtomicReference<>();

    protected int cleanUpExecutedSize = 50;
    protected int maxPollTimeSeconds = 60;

    public DelayedTaskExecutor() {
        this(new FastWaitingExecutor(Java.getAvailableProcessors(), WaitTime.ofSeconds(10)));
    }

    public DelayedTaskExecutor(ExecutorService reaExecutor) {
        this(reaExecutor, 1);
    }

    public DelayedTaskExecutor(ExecutorService realExe, int maxSchedulingThreads) {
        this.realExe = Objects.requireNonNull(realExe);
        if (maxSchedulingThreads < 0) {
            throw new IllegalArgumentException("Max scheduling threads should be at least 1");
        }
        this.maxSchedulingThreads = maxSchedulingThreads;
    }

    boolean cleanUpOneShots() {
        CompletableFuture oneShots = oneShotCompletion.get();
        if (oneShots == null) {
            return false;
        }
        for (DTEScheduledFuture future : dq) {
            if (future != null) {
                if (future.isOneShot()) {
                    return false; // found unfinished oneshot, abort future completion
                }
            }
        }
        oneShots.complete(null);
        oneShotCompletion.compareAndSet(oneShots, null);
        return true;
    }

    boolean cleanUp() {

        Iterator<Future> iterator = executed.iterator();
        boolean complete = true;
        while (iterator.hasNext()) {
            Future fut = iterator.next();
            if (fut == null) {
                continue;
            }
            if (fut.isDone()) {
                iterator.remove();
            } else {
                complete = false;
            }

        }

        if (complete) {
            for (DTEScheduledFuture future : dq) {
                if (future != null) {
                    return false;
                }
            }
        }

        CompletableFuture full = fullCompletion.get();
        if (complete && full != null) {
            full.complete(null);
            fullCompletion.compareAndSet(full, null);
            return true;
        }

        return false;
    }

    @Override
    public void execute(Runnable command) {
        assertShutdown();
        Objects.requireNonNull(command);
        executed.add(realExe.submit(command));
        if (executed.size() > cleanUpExecutedSize) {
            cleanUp();
        }
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

    protected void assertShutdown() {
        if (shutdown) {
            throw new IllegalArgumentException("Shutdown has been called, can't schedule more");
        }
    }

    protected <T extends DTEScheduledFuture<?>> T schedule(T future) {
        assertShutdown();

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
                while (!dq.isEmpty() || !executed.isEmpty()) {

                    if (shutdown) {
                        dq.clear();
                    } else if (!dq.isEmpty()) {
                        DTEScheduledFuture take = dq.poll(maxPollTimeSeconds, TimeUnit.SECONDS);
                        if (take != null) {
                            execute(take);
                        }
                    }
                    if (!executed.isEmpty()) {
                        cleanUp();
                    }

                    cleanUpOneShots();

                }

            } catch (Throwable th) {
                // not our problem
            } finally {

                if ((shutdown && !executed.isEmpty()) || (!shutdown && !dq.isEmpty())) {
                    startSchedulingThread();
                } else {
                    threadCount.decrementAndGet();
                    cleanUpOneShots();
                    cleanUp();
                }

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
        cleanUpOneShots();
        return awaiter;
    }

    public Awaiter awaitFullCompletion() {
        Awaiter awaiter = Awaiter.fromFutureAtomicReference(fullCompletion, CompletableFuture::new);
        cleanUp();
        return awaiter;
    }

    @Override
    public void close() {
        shutdown = true;
        tg.interrupt();
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
    public boolean isShutdown() {
        return shutdown;
    }

    @Override
    public boolean isTerminated() {
        return realExe.isTerminated();
    }

    @Override
    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        return realExe.awaitTermination(timeout, unit);
    }

    @Override
    public ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit) {
        return schedule(WaitTime.of(delay, unit), command);
    }

    @Override
    public <V> ScheduledFuture<V> schedule(Callable<V> callable, long delay, TimeUnit unit) {
        return schedule(WaitTime.of(delay, unit), callable);
    }

    @Override
    public ScheduledFuture<?> scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit) {
        WaitTime time = WaitTime.of(period, unit);
        AtomicReference<ScheduledFuture> ref = new AtomicReference<>();
        Deque<Future> futures = new ArrayDeque<>();
        DTEScheduledFuture schedule = schedule(WaitTime.of(initialDelay, unit), () -> {
            command.run(); // first run
            DTELoopingScheduledFuture scheduledFuture = scheduleWithFixedDelay(time, () -> { // periodically create one-shot task.
                futures.add(schedule(time, command));
                Iterator<Future> iterator = futures.iterator();
                while (iterator.hasNext()) {
                    Future fut = iterator.next();
                    if (fut.isDone()) {
                        iterator.remove();
                    }
                }
            });
            ref.set(scheduledFuture);
        });
        ref.set(schedule);
        futures.add(schedule);
        return (ForwardingScheduledFuture<Object>) new ForwardingScheduledFuture<Object>() {
            @Override
            public ScheduledFuture<Object> delegate() {
                return ref.get();
            }

            @Override
            public boolean cancel(boolean mayInterruptIfRunning) {
                boolean foundOk = false;
                for (Future future : futures) {
                    if (future.cancel(mayInterruptIfRunning)) {
                        foundOk = true;
                    }
                }
                return delegate().cancel(mayInterruptIfRunning) || foundOk;
            }

        };
    }

    @Override
    public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command, long initialDelay, long delay, TimeUnit unit) {
        WaitTime time = WaitTime.of(delay, unit);
        AtomicReference<ScheduledFuture> ref = new AtomicReference<>();
        ref.set(schedule(WaitTime.of(initialDelay, unit), () -> {
            command.run(); // first run
            ref.set(scheduleWithFixedDelay(time, command));
        }));
        return (ForwardingScheduledFuture<Object>) () -> ref.get();
    }
}
