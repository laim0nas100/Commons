package lt.lb.commons.threads.sync;

import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.LinkedTransferQueue;
import lt.lb.commons.ArrayOp;
import lt.lb.commons.F;
import lt.lb.commons.threads.UnsafeRunnable;

/**
 *
 * Bottle neck for threaded execution. Supports recursive calls in the same
 * thread. Same bottleneck can be shared in unrelated execute calls.
 * The execution isn't fair.
 * @author laim0nas100
 */
public class ThreadBottleneck {

    private ThreadLocal<Boolean> inside = ThreadLocal.withInitial(() -> Boolean.FALSE);
    private static final Object dummy = new Object();

    private LinkedTransferQueue q = new LinkedTransferQueue();

    /**
     *
     * @param maxThreads Max amount of threads can be inside a execute call.
     */
    public ThreadBottleneck(int maxThreads) {
        if (maxThreads <= 0) {
            throw new IllegalArgumentException("Thread count must be above zero");
        }
        for (int i = 0; i < maxThreads; i++) {
            q.add(dummy);
        }

    }

    public Optional<Throwable> execute(Callable call, WaitTime time) throws InterruptedException {
        return execute(UnsafeRunnable.from(call), time);
    }

    public Optional<Throwable> execute(Runnable run, WaitTime time) throws InterruptedException {
        return execute(UnsafeRunnable.from(run), time);
    }

    public Optional<Throwable> execute(UnsafeRunnable run, WaitTime time) throws InterruptedException {
        if (inside.get()) { // recursive call
            return F.checkedRun(run);
        }
        Object poll = q.poll(time.time, time.unit);
        if (poll != null) {
            return uniqueThreadRun(run);
        } else {
            throw new IllegalStateException("Got null from queue. Should not happen");
        }
    }

    private Optional<Throwable> uniqueThreadRun(UnsafeRunnable r) {
        inside.set(Boolean.TRUE);
        Optional<Throwable> result = F.checkedRun(r);
        if (!q.tryTransfer(dummy)) { // maybe there is a thread already waiting, so we try quick add
            if (!q.add(dummy)) {
                throw new IllegalStateException("Failed to reinsert. Should not happen");
            }
        }
        inside.set(Boolean.FALSE);
        return result;
    }

    public Optional<Throwable> execute(Callable call) {
        return execute(UnsafeRunnable.from(call));
    }

    public Optional<Throwable> execute(Runnable run) {
        return execute(UnsafeRunnable.from(run));
    }

    public Optional<Throwable> execute(UnsafeRunnable run) {
        if (inside.get()) { // recursive call
            return F.checkedRun(run);
        }
        Object poll = null;
        try {
            poll = q.take();
        } catch (InterruptedException ex) {
            return Optional.of(ex);
        }
        if (poll != null) {
            return uniqueThreadRun(run);
        } else {
            throw new IllegalStateException("Got null from queue. Should not happen");
        }
    }

}
