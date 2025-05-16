package lt.lb.commons.threads.executors;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Future;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.TimeUnit;
import static java.util.concurrent.TimeUnit.NANOSECONDS;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import lt.lb.commons.F;
import lt.lb.commons.Nulls;
import lt.lb.commons.threads.SimpleThreadPool;
import lt.lb.commons.threads.ThreadPool;
import lt.lb.commons.threads.sync.ConcurrentConsume;
import lt.lb.uncheckedutils.concurrent.ThreadLocalParkSpace;

/**
 *
 * Spawns new threads on demand. If all tasks are exhausted, thread terminates
 * immediately.
 *
 * Max threads parameter:<br> negative = unlimited threads;<br> 0 - no threads,
 * execution in the same thread;<br> positive - bounded threads.
 *
 * Must call {@code Burst} to actually begin executing
 *
 * @author laim0nas100
 */
public class BurstExecutor extends BaseExecutor implements CloseableExecutor {

    protected static class BurstBatch extends ConcurrentConsume<Runnable> {

        public BurstBatch() {
            super(128);
        }

        public BurstBatch(int size) {
            super(size);
        }

        public List<Runnable> consumeAll() {
            List<Runnable> consumed = new ArrayList<>();
            Runnable c = consume();
            while (c != null) {
                consumed.add(c);
                c = consume();

            }
            return consumed;
        }

    }

    protected ConcurrentLinkedQueue<BurstBatch> bursts = new ConcurrentLinkedQueue<>();
    protected BurstBatch currentBurst = new BurstBatch();

    protected ThreadLocalParkSpace<Running> runningTasks;

    protected ThreadPool pool;

    protected final int maxThreads;
    protected AtomicInteger occupiedThreads = new AtomicInteger(0);
    protected CompletableFuture awaitTermination = new CompletableFuture();

    protected Consumer<Throwable> errorChannel = (err) -> {
    };

    /**
     *
     * @param maxThreads positive limited threads negative unlimited threads
     * zero no threads, execute during update
     *
     */
    public BurstExecutor(int maxThreads) {
        this(maxThreads, new SimpleThreadPool(BurstExecutor.class));
    }

    protected BurstExecutor(int maxThreads, ThreadPool threadPool) {
        this.pool = Nulls.requireNonNull(threadPool, "threadPool must not be null");
        if (maxThreads < 0) {
            throw new IllegalArgumentException("Unbounded amount of threads is not supported");
        }
        this.maxThreads = maxThreads;
        this.runningTasks = new ThreadLocalParkSpace<>(Math.max(8, (int) (maxThreads * 2.72)));
//        this.runningTasks = new ConcurrentIndexedBag<>(Math.max(8, (int) (maxThreads * 2.72)));// e approximation

        pool.setStarting(true);
    }

    public void setErrorChannel(Consumer<Throwable> channel) {
        Nulls.requireNonNull(channel, "Error channel must not be null");
        errorChannel = channel;
    }

    public boolean isDeamon() {
        return pool.isDaemon();
    }

    /**
     * Updates all active thread status and every newly spawned thread will be
     * of newly updated status
     *
     * @param deamon
     */
    public void setDeamon(boolean deamon) {
        pool.setDaemon(deamon);
    }

    public Consumer<Throwable> getErrorChannel() {
        return errorChannel;
    }

    protected void execute(BurstBatch providedBurst, Runnable command) {
        if (!open) {
            throw new IllegalStateException("Not open");
        }
        Objects.requireNonNull(command, "null runnable recieved");
        if (maxThreads == 0) {
            executeSingle(command);
        } else {
            BurstBatch burst = Nulls.requireNonNullElse(providedBurst, currentBurst);
            if (!burst.add(command)) {
                throw new IllegalStateException("Failed to submit runnable, burst command must be in the same thread as execute");
            }

        }
    }

    @Override
    public void execute(Runnable command) {
        execute(null, command);
    }

    @Override
    public int parallelism() {
        return maxThreads;
    }

    public void burst() {
        burst(null);
    }

    protected void burst(BurstBatch burst) {
        if (burst == null) {
            burst = currentBurst;
            currentBurst = new BurstBatch();
        }
        burst.readOnly();
        bursts.add(burst);
        if (maxThreads == 0) {
            for (;;) {
                BurstBatch poll = bursts.poll();
                if (poll == null) {
                    if (bursts.isEmpty()) {
                        break;
                    }

                } else {
                    polling(poll, false);
                }
            }
        }
        int howMany = Math.min(maxThreads, burst.unfinished());
        for (int i = 0; i < howMany; i++) {
            boolean ok = maybeStartThread(burst);
            if (!ok) {
                return;// allready running
            }
        }
    }

    protected void polling(BurstBatch burst, final boolean parking) {

        final Running running = parking ? new Running(Thread.currentThread(), null) : Running.EMPTY;
        int index = parking ? runningTasks.park(running) : -1;
        try {
            for (;;) {
                Runnable run = burst.consume();
                if (run == null) {
                    return;
                } else {
                    if (parking) {
                        if (running.canceled) {
                            return;
                        }
                        running.runnable = run;
                    }

                    executeSingle(run);
                }
            }
        } finally {
            if (index >= 0) {
                runningTasks.unpark(index);
            }
        }
    }

    protected final void executeSingle(Runnable run) {
        try {
            run.run();

        } catch (Throwable th) {
            try {
                getErrorChannel().accept(th);
            } catch (Throwable err) {// we are really screwed now
                err.printStackTrace();
            }
        }
    }

    protected final Runnable threadBody(BurstBatch burst) {
        return () -> {

            try {
                polling(burst, true);
            } finally {
                int leftRunning = occupiedThreads.decrementAndGet(); //thread no longer running (not really)

                if (leftRunning == 0) {
                    BurstBatch burstFound = null;
                    if (!bursts.isEmpty()) { //last thread, look for new burst

                        for (;;) {
                            BurstBatch poll = bursts.poll();
                            if (poll == null) {
                                if (bursts.isEmpty()) {
                                    break;
                                }

                            } else {
                                if (poll.unfinished() > 0) {
                                    burstFound = poll;
                                    break;
                                }
                            }
                        }

                    }
                    if (burstFound != null) {
                        burst(burstFound);
                    } else {
                        maybeTerminate(0);
                    }
                }
            }
        };
    }

    protected void maybeTerminate(int leftRunning) {
        if (!open && leftRunning == 0) {
            awaitTermination.complete(0);
        }
    }

    protected Thread startThread(BurstBatch burst) {
        return pool.newThread(threadBody(burst));//pool should start the thread
    }

    protected boolean maybeStartThread(BurstBatch burst) {
        if (maxThreads > 0) {// limitedThreads
            if (occupiedThreads.get() >= maxThreads) {//fast exit
                return false;
            }
            if (occupiedThreads.incrementAndGet() > maxThreads) {
                occupiedThreads.decrementAndGet();
                return false;
            }
            startThread(burst);
            return true;

        } //unsuported amount of threads
        throw new IllegalStateException("Unlimited or zero threads is not supported to start a new thread");

    }

    public List<Runnable> cancelAll(boolean interrupting) {
        ArrayList<Runnable> unfinished = new ArrayList<>();

        BurstBatch burst = currentBurst;
        currentBurst = new BurstBatch();
        burst.readOnly();
        unfinished.addAll(burst.consumeAll());

        Iterator<BurstBatch> iterator = bursts.iterator();
        while (iterator.hasNext()) {
            BurstBatch next = iterator.next();
            iterator.remove();
            if (next != null) {
                unfinished.addAll(next.consumeAll());
            }
        }

        if (interrupting) {
            for (Running r : runningTasks) {
                if (r != null) {
                    if (r.thread.isAlive()) {
                        r.thread.interrupt();
                    }
                }
            }
        }

        return unfinished;
    }

    @Override
    public List<Runnable> shutdownNow() {
        shutdown();
        List<Runnable> unfinished = cancelAll(true);
        maybeTerminate(occupiedThreads.get());

        return unfinished;
    }

    protected Iterator<Running> getRunningTasks() {
        return runningTasks.iterator();
    }

    public boolean isBusy() {
        return occupiedThreads.get() > 0;
    }

    @Override
    public boolean isTerminated() {
        return awaitTermination.isDone();
    }

    @Override
    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        try {
            if (isTerminated()) {
                return true;
            }
            maybeTerminate(occupiedThreads.get());
            this.awaitTermination.get(timeout, unit);
            return true;
        } catch (ExecutionException exe) {
            throw new Error("Should never happen", exe);
        } catch (TimeoutException ex) {
            return false; // too late
        }

    }

    /**
     * Threads close automatically when all tasks are exhausted. This method
     * ensures no more runnable`s gets submitted. Does not actually wait for
     * threads to close.
     */
    @Override
    public void shutdown() {
        this.open = false;
        maybeTerminate(occupiedThreads.get());
    }

    private <T> T doInvokeAny(Collection<? extends Callable<T>> tasks,
            boolean timed, long nanos)
            throws InterruptedException, ExecutionException, TimeoutException {
        if (tasks == null) {
            throw new NullPointerException();
        }
        int ntasks = tasks.size();
        if (ntasks == 0) {
            throw new IllegalArgumentException();
        }
        ArrayList<Future<T>> futures = new ArrayList<>(ntasks);
        ExecutorCompletionService<T> ecs
                = new ExecutorCompletionService<T>(this);

        // For efficiency, especially in executors with limited
        // parallelism, check to see if previously submitted tasks are
        // done before submitting more of them. This interleaving
        // plus the exception mechanics account for messiness of main
        // loop.
        try {
            // Record exceptions so that if we fail to obtain any
            // result, we can throw the last exception we got.
            ExecutionException ee = null;
            final long deadline = timed ? System.nanoTime() + nanos : 0L;
            Iterator<? extends Callable<T>> it = tasks.iterator();

            // Start one task for sure; the rest incrementally
            futures.add(ecs.submit(it.next()));
            --ntasks;
            int active = 1;

            for (;;) {
                Future<T> f = ecs.poll();
                if (f == null) {
                    if (ntasks > 0) {
                        --ntasks;
                        futures.add(ecs.submit(it.next()));
                        ++active;
                    } else if (active == 0) {
                        break;
                    } else if (timed) {
                        burst(null);
                        f = ecs.poll(nanos, NANOSECONDS);
                        if (f == null) {
                            throw new TimeoutException();
                        }
                        nanos = deadline - System.nanoTime();
                    } else {
                        burst(null);
                        f = ecs.take();
                    }
                }
                if (f != null) {
                    --active;
                    try {
                        return f.get();
                    } catch (ExecutionException eex) {
                        ee = eex;
                    } catch (RuntimeException rex) {
                        ee = new ExecutionException(rex);
                    }
                }
            }

            if (ee == null) {
                ee = new ExecutionException(new IllegalStateException("Failed without exception"));
            }
            throw ee;

        } finally {
            cancelAll(futures);
        }
    }

    /**
     * @throws InterruptedException {@inheritDoc}
     * @throws NullPointerException {@inheritDoc}
     * @throws IllegalArgumentException {@inheritDoc}
     * @throws ExecutionException {@inheritDoc}
     * @throws RejectedExecutionException {@inheritDoc}
     */
    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks)
            throws InterruptedException, ExecutionException {
        try {
            return doInvokeAny(tasks, false, 0);
        } catch (TimeoutException cannotHappen) {
            assert false;
            return null;
        }
    }

    /**
     * @throws InterruptedException {@inheritDoc}
     * @throws NullPointerException {@inheritDoc}
     * @throws TimeoutException {@inheritDoc}
     * @throws ExecutionException {@inheritDoc}
     * @throws RejectedExecutionException {@inheritDoc}
     */
    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks,
            long timeout, TimeUnit unit)
            throws InterruptedException, ExecutionException, TimeoutException {
        return doInvokeAny(tasks, true, unit.toNanos(timeout));
    }

    /**
     * @throws InterruptedException {@inheritDoc}
     * @throws NullPointerException {@inheritDoc}
     * @throws RejectedExecutionException {@inheritDoc}
     */
    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks)
            throws InterruptedException {
        if (tasks == null) {
            throw new NullPointerException();
        }
        ArrayList<Future<T>> futures = new ArrayList<>(tasks.size());
        BurstBatch burst = new BurstBatch(tasks.size());
        try {
            for (Callable<T> t : tasks) {
                RunnableFuture<T> f = newTaskFor(t);
                futures.add(f);
                execute(burst, f);
            }
            burst(burst);

            for (int i = 0, size = futures.size(); i < size; i++) {
                Future<T> f = futures.get(i);
                if (!f.isDone()) {
                    try {
                        f.get();
                    } catch (CancellationException | ExecutionException ignore) {
                    }
                }
            }
            return futures;
        } catch (Throwable t) {
            cancelAll(futures);
            throw t;
        }
    }

    /**
     * @throws InterruptedException {@inheritDoc}
     * @throws NullPointerException {@inheritDoc}
     * @throws RejectedExecutionException {@inheritDoc}
     */
    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks,
            long timeout, TimeUnit unit)
            throws InterruptedException {
        if (tasks == null) {
            throw new NullPointerException();
        }
        final long nanos = unit.toNanos(timeout);
        final long deadline = System.nanoTime() + nanos;
        ArrayList<RunnableFuture<T>> futures = new ArrayList<>(tasks.size());
        BurstBatch burst = new BurstBatch(tasks.size());
        int j = 0;
        timedOut:
        try {
            for (Callable<T> t : tasks) {
                futures.add(newTaskFor(t));
            }

            final int size = futures.size();

            for (int i = 0; i < size; i++) {
                execute(burst, futures.get(i));
            }
            burst(burst);

            for (; j < size; j++) {
                Future<T> f = futures.get(j);
                if (!f.isDone()) {
                    try {
                        f.get(deadline - System.nanoTime(), NANOSECONDS);
                    } catch (CancellationException | ExecutionException ignore) {
                    } catch (TimeoutException timedOut) {
                        break timedOut;
                    }
                }
            }
            return F.cast(futures);
        } catch (Throwable t) {
            cancelAll(F.cast(futures));
            throw t;
        }
        // Timed out before all the tasks could be completed; cancel remaining
        cancelAll(F.cast(futures), j);
        return F.cast(futures);
    }

}
