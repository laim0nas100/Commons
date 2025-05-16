package lt.lb.commons.threads.executors;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import lt.lb.commons.F;
import lt.lb.commons.Nulls;
import lt.lb.commons.threads.SimpleThreadPool;
import lt.lb.commons.threads.ThreadPool;
import lt.lb.commons.threads.sync.ConcurrentIndexedAbstract;
import lt.lb.commons.threads.sync.ConcurrentIndexedBag;
import lt.lb.uncheckedutils.concurrent.ThreadLocalParkSpace;

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
@Deprecated
public class FastExecutorOld extends AbstractExecutorService implements CloseableExecutor {

    protected static class Running {

        public final Thread thread;
        public final Runnable runnable;

        public Running(Thread thread, Runnable runnable) {
            this.thread = thread;
            this.runnable = runnable;
        }
    }

    protected Collection<Runnable> tasks = new ConcurrentLinkedDeque<>();

//    protected ConcurrentIndexedAbstract<Running, ?> runningTasks;
     protected ThreadLocalParkSpace<Running> runningTasks;

    protected ThreadPool pool;

    protected volatile boolean open = true;
    protected int maxThreads;
    protected AtomicInteger startingThreads = new AtomicInteger(0);
    protected AtomicInteger runningThreads = new AtomicInteger(0);
    protected AtomicInteger finishingThreads = new AtomicInteger(0);
    protected CompletableFuture awaitTermination = new CompletableFuture();

    protected Consumer<Throwable> errorChannel = (err) -> {
    };

    /**
     *
     * @param maxThreads positive limited threads negative unlimited threads
     * zero no threads, execute during update
     *
     */
    public FastExecutorOld(int maxThreads) {
        this(maxThreads, new SimpleThreadPool(FastExecutorOld.class));
    }

    protected FastExecutorOld(int maxThreads, ThreadPool threadPool) {
        this.pool = Nulls.requireNonNull(threadPool, "threadPool must not be null");
        this.maxThreads = maxThreads;
        this.runningTasks = new ThreadLocalParkSpace<>(Math.max(8, maxThreads));
//        this.runningTasks = new ConcurrentIndexedBag<>(Math.max(8, (int) (maxThreads * 2.72)));// e approximation

        pool.setStarting(true);
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
            tasks.add(command);
            update(this.maxThreads);
        }

    }

    protected void polling() {
        Deque<Runnable> deque = F.cast(tasks);
        while (!deque.isEmpty()) {
            Runnable run = deque.pollFirst();
            if(run == null){
                return;
            }
            executeSingle(run, true);
        }
    }

    protected final void executeSingle(Runnable run, boolean useList) {
        if (run == null) {
            return;
        }
        int index = -1;
        try {
            if (useList) {
//                index = runningTasks.insert(new Running(Thread.currentThread(), run));
                index = runningTasks.park(new Running(Thread.currentThread(), run));
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
    }

    protected final Runnable threadBody(final int bakedMax) {
        return () -> {
            runningThreads.incrementAndGet();
            startingThreads.decrementAndGet(); //thread started
            try {
                polling();
            } finally {
                finishingThreads.incrementAndGet(); // thread is finishing
                runningThreads.decrementAndGet(); //thread no longer running (not really)
                update(bakedMax);
                finishingThreads.decrementAndGet(); // thread end finishing
                maybeTerminate();
            }
        };
    }

    protected void maybeTerminate() {
        if (!open && runningThreads.get() == 0 && startingThreads.get() == 0 && finishingThreads.get() == 0) {
            awaitTermination.complete(0);
        }
    }

    protected void update(int maxT) {
        if (tasks.isEmpty()) {
            return;
        }
        this.maybeStartThread(maxT);

    }

    protected Thread startThread(final int maxT) {
        return pool.newThread(threadBody(maxT));//pool should start the thread
    }

    protected void maybeStartThread(final int maxT) {

        if (maxT == 0) {
            throw new IllegalArgumentException("Max threads is 0");
        } else if (maxT < 0) {//unlimited threads
            startingThreads.incrementAndGet();//because run decrements
            startThread(maxT);
        } else { // limitedThreads
            final int starting = startingThreads.incrementAndGet();
            if (starting > maxT) { // we dispatch threads too often
                this.startingThreads.decrementAndGet();
            } else if (starting + runningThreads.get() <= maxT) { // we are within limit
                startThread(maxT);
            } else { // don't start new thread, just update value
                this.startingThreads.decrementAndGet();
            }
        }
    }

    /**
     * Threads close automatically when all tasks are exhausted. This method
     * ensures no more runnable's gets submitted. Does not actually wait for
     * threads to close.
     */
    @Override
    public void close() {
        this.open = false;
        maybeTerminate();
    }

    @Override
    public List<Runnable> shutdownNow() {
        shutdown();

        Iterator<Running> running = runningTasks.iterator();
        while (running.hasNext()) {
            Running r = running.next();
            if (r != null) {
                r.thread.interrupt();
            }
        }
        ArrayList<Runnable> unfinished = new ArrayList<>();
        Iterator<Runnable> iterator = tasks.iterator();
        while (iterator.hasNext()) {
            Runnable next = iterator.next();
            iterator.remove();
            if (next != null) {
                unfinished.add(next);
            }
        }

        maybeTerminate();

        return unfinished;
    }

    protected Iterator<Running> getRunningTasks() {
        return runningTasks.iterator();
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
