package lt.lb.commons.threads.executors;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;
import lt.lb.commons.threads.ThreadPool;

/**
 * In-place executor. All semantics of executor service, but executes tasks in
 * the same thread as they are submitted. Not to be shared across threads,
 *
 * No cleanup is required.
 *
 * @author laim0nas100
 */
public class InPlaceExecutor extends BaseExecutor {

    protected CompletableFuture terminated = new CompletableFuture();
    protected AtomicReference<Running> running = new AtomicReference<>(null);

    protected final boolean sameThreadShutdown;

    /**
     *
     * @param sameThreadShutdown if expecting a shutdown from different thread,
     * more house-keeping overhead
     */
    public InPlaceExecutor(boolean sameThreadShutdown) {
        this.sameThreadShutdown = sameThreadShutdown;
    }

    public InPlaceExecutor() {
        this(true);
    }

    @Override
    public int parallelism() {
        return 0;
    }

    @Override
    public void shutdown() {
        open = false;
        if (running.compareAndSet(null, null)) {
            terminated.complete(0);
        }
    }

    /**
     * could shutdown from another thread
     *
     * @return
     */
    @Override
    public List<Runnable> shutdownNow() {
        shutdown();
        ArrayList<Runnable> arrayList = new ArrayList<>();
        Running get = running.get();
        if (get != null) {
            if (ThreadPool.threadIsWaiting(get.thread)) {
                get.thread.interrupt();
                arrayList.add(get.runnable);
            }
        }
        return arrayList;
    }

    @Override
    public boolean isTerminated() {
        return terminated.isDone();
    }

    @Override
    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        try {
            terminated.get(timeout, unit);
            return true;
        } catch (ExecutionException | TimeoutException ex) {
            return false;
        }

    }

    @Override
    public void execute(Runnable command) {
        if (!open) {
            throw new IllegalStateException("InPlaceExecutor is not open");
        }
        Objects.requireNonNull(command);
        if (sameThreadShutdown) {
            command.run();
            return;
        }
        Running run = new Running(Thread.currentThread(), command);
        if (running.compareAndSet(null, run)) {
            try {
                run.runnable.run();
            } finally {
                running.set(null);
                if (isShutdown()) {
                    terminated.complete(0);
                }
            }
        } else {
            throw new IllegalStateException("InPlaceExecutor is being used by another thread");
        }

    }

}
