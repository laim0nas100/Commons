package lt.lb.commons.threads.executors;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import lt.lb.commons.F;
import lt.lb.commons.Nulls;
import lt.lb.commons.threads.SimpleThreadPool;
import lt.lb.commons.threads.ThreadPool;
import lt.lb.commons.threads.sync.ThreadLocalParkSpace;

/**
 *
 * Spawns new threads on demand. If all tasks are exhausted, thread terminates
 * immediately.
 *
 * Max threads parameter:<br> negative = unlimited threads;<br> 0 - no threads,
 * execution in the same thread;<br> positive - bounded threads
 *
 * @author laim0nas100
 */
public class FastExecutor extends AbstractExecutorService implements CloseableExecutor {

    protected static class Running {

        public final Thread thread;
        public final Runnable runnable;

        public Running(Thread thread, Runnable runnable) {
            this.thread = thread;
            this.runnable = runnable;
        }
    }

    protected Queue<Runnable> tasks;

    protected ThreadLocalParkSpace<Running> runningTasks;

    protected ThreadPool pool;

    protected AtomicInteger adds = new AtomicInteger(0);
    protected volatile boolean open = true;
    protected int maxThreads;
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
    public FastExecutor(int maxThreads) {
        this(maxThreads, new SimpleThreadPool(FastExecutor.class));
    }

    protected FastExecutor(int maxThreads, ThreadPool threadPool) {
        this.pool = Nulls.requireNonNull(threadPool, "threadPool must not be null");
        this.maxThreads = maxThreads;
        this.runningTasks = new ThreadLocalParkSpace<>(Math.max(8, (int) (maxThreads * 2.72)));
//        this.runningTasks = new ConcurrentIndexedBag<>(Math.max(8, (int) (maxThreads * 2.72)));// e approximation

        pool.setStarting(true);
        tasks = makeQueue();
    }

    protected Queue<Runnable> makeQueue() {
        if (maxThreads == 0) {
            return null;
        }
        if (maxThreads > 0 && maxThreads <= 2) {
            return new ConcurrentLinkedQueue<>();
        }
        return new LinkedBlockingQueue<>();
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
            executeSingle(command, false);
        } else {
            adds.incrementAndGet();
            if (!tasks.add(command)) {
                adds.decrementAndGet();
                throw new IllegalStateException("Failed to submit runnable, queue overflow");

            }
            maybeStartThread(maxThreads);
        }

    }

    protected void polling() {
        Queue<Runnable> queue = F.cast(tasks);

        int index = -1;
        while (true) {
            Runnable run = queue.poll();
            if (run == null) {
                return;
            } else {
                adds.decrementAndGet();
            }
            index = executeSingle(index, run, true);
        }
    }

    protected final int executeSingle(Runnable run, boolean useList) {
        return executeSingle(-1, run, useList);
    }

    protected final int executeSingle(int index, Runnable run, boolean useList) {
        if (run == null) {
            return -1;
        }
        try {
            if (useList) {
//                index = runningTasks.insert(new Running(Thread.currentThread(), run));
                boolean parked = false;
                if (index >= 0) {
                    parked = runningTasks.park(index, new Running(Thread.currentThread(), run));

                }
                if (!parked) {
                    index = runningTasks.park(new Running(Thread.currentThread(), run));
                }

            }
            run.run();

        } catch (Throwable th) {
            try {
                getErrorChannel().accept(th);
            } catch (Throwable err) {// we are really screwed now
                err.printStackTrace();
            }
        } finally {
            if (useList && index >= 0) {
                runningTasks.unpark(index);
//                runningTasks.remove(index);
            }
        }
        return index;
    }

    protected final Runnable threadBody(final int bakedMax) {
        return () -> {

//            runningThreads.incrementAndGet();
            try {
                polling();
            } finally {
//                occupiedThreads.decrementAndGet();
                int leftRunning = occupiedThreads.decrementAndGet(); //thread no longer running (not really)
                int get = adds.get();
                if (!open && get == 0) {
                    maybeTerminate(leftRunning);
                } else if (get > 0) {
                    maybeStartThread(bakedMax);
                }

            }
        };
    }

    protected void maybeTerminate(int leftRunning) {
        if (!open && leftRunning == 0) {
            awaitTermination.complete(0);
        }
    }

    protected Thread startThread(final int maxT) {
        return pool.newThread(threadBody(maxT));//pool should start the thread
    }

    protected void maybeStartThread(final int maxT) {
        if (maxT > 0) {// limitedThreads
            if (occupiedThreads.get() >= maxT) {//fast exit
                return;
            }
            if (occupiedThreads.incrementAndGet() > maxT) {
                occupiedThreads.decrementAndGet();
            } else {
                startThread(maxT);
            }
        } else if (maxT == 0) {
            throw new IllegalArgumentException("Max threads is 0");
        } else {//unlimited threads
            occupiedThreads.incrementAndGet();
            startThread(maxT);
        } 
    }

    /**
     * Threads close automatically when all tasks are exhausted. This method
     * ensures no more runnable`s gets submitted. Does not actually wait for
     * threads to close.
     */
    @Override
    public void close() {
        this.open = false;
        maybeTerminate(occupiedThreads.get());
    }

    public List<Runnable> cancelAll(boolean interrupting) {
        ArrayList<Runnable> unfinished = new ArrayList<>();
        Iterator<Runnable> iterator = tasks.iterator();
        while (iterator.hasNext()) {
            Runnable next = iterator.next();
            iterator.remove();
            if (next != null) {
                unfinished.add(next);
            }
        }

        if (interrupting) {
            Iterator<Running> running = runningTasks.iterator();
            while (running.hasNext()) {
                Running r = running.next();
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
    public boolean isShutdown() {
        return !open;
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

    @Override
    public void shutdown() {
        close();
    }

}
