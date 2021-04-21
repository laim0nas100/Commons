package lt.lb.commons.threads.sync;

import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.TransferQueue;
import lt.lb.commons.F;
import lt.lb.uncheckedutils.Checked;
import lt.lb.uncheckedutils.func.UncheckedRunnable;

/**
 *
 * Bottle neck for threaded execution. Supports recursive calls in the same
 * thread. Same bottleneck can be shared in unrelated execute calls. The
 * execution isn't fair.
 *
 * @author laim0nas100
 */
public class ThreadBottleneck {

    protected ThreadLocal<Boolean> inside = ThreadLocal.withInitial(() -> Boolean.FALSE);
    protected static final Object dummy = new Object();

    protected BlockingQueue q = new LinkedTransferQueue();

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
        return execute(UncheckedRunnable.from(call), time);
    }

    public Optional<Throwable> execute(Runnable run, WaitTime time) throws InterruptedException {
        return execute(UncheckedRunnable.from(run), time);
    }

    public Optional<Throwable> execute(UncheckedRunnable run, WaitTime time) throws InterruptedException {
        if (inside.get()) { // recursive call
            return Checked.checkedRun(run);
        }
        Object poll = tryTakeToken(time);
        if (poll != null) {
            return uniqueThreadRun(run);
        } else {
            throw new IllegalStateException("Got null from queue. Should not happen");
        }
    }

    protected void reinsert() {
        TransferQueue ltq = F.cast(q);
        if (!ltq.tryTransfer(dummy)) { // maybe there is a thread already waiting, so we try quick add
            if (!ltq.add(dummy)) {
                throw new IllegalStateException("Failed to reinsert. Should not happen");
            }
        }
    }

    private Optional<Throwable> uniqueThreadRun(UncheckedRunnable r) {
        inside.set(Boolean.TRUE);
        Optional<Throwable> result = Checked.checkedRun(r);
        reinsert();
        inside.set(Boolean.FALSE);
        return result;
    }

    protected Object tryTakeToken() throws InterruptedException {
        return q.poll();
    }
    
    protected Object tryTakeToken(WaitTime wt) throws InterruptedException {
        return q.poll(wt.time, wt.unit);
    }

    public Optional<Throwable> execute(Callable call) {
        return execute(UncheckedRunnable.from(call));
    }

    public Optional<Throwable> execute(Runnable run) {
        return execute(UncheckedRunnable.from(run));
    }

    private Optional<Throwable> execute0(UncheckedRunnable run) {
        if (inside.get()) { // recursive call
            return Checked.checkedRun(run);
        }
        Object poll = null;
        try {
            poll = tryTakeToken();
        } catch (InterruptedException ex) {
            return Optional.of(ex);
        }
        if (poll != null) {
            return uniqueThreadRun(run);
        } else {
            throw new IllegalStateException("Got null from queue. Should not happen");
        }
    }

    public Optional<Throwable> execute(UncheckedRunnable run) {
        return execute0(run);
    }

}
