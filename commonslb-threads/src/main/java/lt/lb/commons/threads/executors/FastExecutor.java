package lt.lb.commons.threads.executors;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.LockSupport;
import java.util.function.Consumer;
import lt.lb.commons.Nulls;
import lt.lb.commons.misc.numbers.Atomic;
import lt.lb.commons.threads.SourcedThreadPool;
import lt.lb.commons.threads.sync.ConcurrentArena;

/**
 *
 * Spawns new threads on demand. If all tasks are exhausted, thread terminates
 * immediately.
 *
 * Max threads parameter:<br> negative = unlimited threads;<br> 0 - no threads,
 * execution in the same thread;<br> positive - bounded threads; <br>
 *
 * Sub-par performance for a lot of small tasks at once due to thread congestion
 * while using 3 or more active threads. For such cases use
 * {@link BurstExecutor}.<br>
 *
 * Task submit performance exceeds the jdk default.
 *
 * @author laim0nas100
 */
public class FastExecutor extends BaseExecutor {

    protected ConcurrentArena<Runnable> tasks;

    protected SourcedThreadPool pool;

    protected int maxThreads;
    protected AtomicInteger occupiedThreads = new AtomicInteger(0);
    protected CompletableFuture awaitTermination = new CompletableFuture();
    protected int spec = -1;

    protected Consumer<Throwable> errorChannel = (err) -> {
    };

    /**
     *
     * @param maxThreads positive limited threads negative unlimited threads
     * zero no threads, execute during update
     *
     */
    private FastExecutor(int maxThreads, int spec) {
        this(maxThreads, new SourcedThreadPool(FastExecutor.class));
        this.spec = spec;
        tasks = makeQueue(spec);
    }

    public static FastExecutor _spec(int maxThreads, int spec) {
        return new FastExecutor(maxThreads, spec);
    }

    /**
     *
     * @param maxThreads positive limited threads negative unlimited threads
     * zero no threads, execute during update
     *
     */
    public FastExecutor(int maxThreads) {
        this(maxThreads, new SourcedThreadPool(FastExecutor.class));
    }

    protected FastExecutor(int maxThreads, SourcedThreadPool threadPool) {
        this.pool = Nulls.requireNonNull(threadPool, "threadPool must not be null");
        this.maxThreads = maxThreads;
        pool.setStarting(true);

        tasks = makeQueue();
    }

    protected ConcurrentArena<Runnable> makeQueue(int spec) {
        if (maxThreads == 0) {
            return null;
        }
        if (spec == 0 || maxThreads < 0 || maxThreads > 2) {
            return ConcurrentArena.fromBlocking(new LinkedBlockingQueue<>());
        }
        if (spec == 1) {
            return new ConcurrentArena.ArraySinchronizedArena<>();
        }
        if (spec == 2) {
            return new ConcurrentArena.ArrayLockedArena<>();
        }
        return ConcurrentArena.fromConcurrent(new ConcurrentLinkedQueue<>());
    }

    protected ConcurrentArena<Runnable> makeQueue() {
        return makeQueue(spec);
    }

    public void setMaxThreads(int maxThreads) {
        this.maxThreads = maxThreads;
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

    @Override
    public void execute(Runnable command) {
        if (!open) {
            throw new IllegalStateException("Not open");
        }
        Objects.requireNonNull(command, "null runnable recieved");
        if (maxThreads == 0) {
            executeSingle(command);
        } else {
            if (!maybeStartThread(maxThreads, command)) {
                if (!tasks.add(command)) {
                    throw new IllegalStateException("Failed to submit runnable, queue overflow");

                }
            }
        }

    }

    @Override
    public void executeAll(Collection<Runnable> all) {
        if (maxThreads == 0) {
            for (Runnable r : all) {
                execute(r);
            }
        } else {
            tasks.addAll(all);

            int howMany = maxThreads < 0 ? all.size() : Math.min(maxThreads, all.size());
            for (int i = occupiedThreads.get(); i < howMany; i++) {
                maybeStartThread(maxThreads, null);
            }
        }
    }

    protected Runnable getNext() throws InterruptedException {
        int tries = 3;
        while (--tries >= 0) {
            Runnable poll = tasks.poll();
            if (poll != null) {
                return poll;
            }
            LockSupport.parkNanos(1);
        }
        return null;
    }

    protected void polling(Runnable run) {
        try {

            for (;;) {
                executeSingle(run);// null check inside
                run = getNext();
                if (run == null) {
                    return;
                } else {
                    executeSingle(run);
                }
            }
        } catch (InterruptedException ex) {
        }
    }

    protected final void executeSingle(Runnable run) {
        if (run == null) {
            return;
        }
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

    protected final Runnable threadBody(final int bakedMax, Runnable first) {
        return () -> {

            try {
                polling(first);
            } finally {
                int leftRunning = Atomic.decrementAndGet(occupiedThreads); //thread no longer running (not really)
                int get = tasks.size();
                if (!open && get == 0) {
                    maybeTerminate(leftRunning);
                } else if (get > 0) {
                    maybeStartThread(bakedMax, null);
                }

            }
        };
    }

    protected void maybeTerminate(int leftRunning) {
        if (!open && leftRunning == 0) {
            awaitTermination.complete(0);
        }
    }

    protected Thread startThread(final int maxT, Runnable first) {
        return pool.newThread(threadBody(maxT, first));//pool should start the thread
    }

    protected boolean maybeStartThread(final int maxT, Runnable first) {
        if (maxT > 0) { // limitedThreads
            if (occupiedThreads.get() >= maxT) {//fast exit
                return false;
            }
            if (Atomic.incrementAndGet(occupiedThreads) > maxT) {
                Atomic.decrementAndGet(occupiedThreads);
            } else {
                startThread(maxT, first);
                return true;
            }
        } else if (maxT == 0) {
            throw new IllegalArgumentException("Max threads is 0");
        } else {//unlimited threads
            Atomic.incrementAndGet(occupiedThreads);
            startThread(maxT, first);
            return true;
        }
        return false;
    }

    public List<Runnable> cancelAll(boolean interrupting) {

        ArrayList<Runnable> unfinished = new ArrayList<>(tasks.size());
        Iterator<Runnable> iterator = tasks.iterator();
        while (iterator.hasNext()) {
            Runnable next = iterator.next();
            iterator.remove();
            if (next != null) {
                unfinished.add(next);
            }
        }
        if (interrupting) {
            pool.interruptAlive();
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
        } catch (ExecutionException | TimeoutException ex) {
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

    @Override
    public int parallelism() {
        return maxThreads;
    }

}
