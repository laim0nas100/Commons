package lt.lb.commons.threads.sync;

import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.ReentrantLock;

/**
 *
 * @author laim0nas100
 */
public class SnapshottingAwaiter implements Awaiter {

    private final ArrayDeque<CompletableFuture> futures = new ArrayDeque<>();
    private final ReentrantLock lock = new ReentrantLock(true);

    private final ThreadLocal<CompletableFuture> tl = ThreadLocal.withInitial(() -> null);

    @Override
    public void await(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        CompletableFuture local = tl.get();
        if (local != null) {

            tl.set(null);
            local.get(timeout, unit);
            return;
        }
        CompletableFuture future = new CompletableFuture();
        lock.lock();
        try {
            futures.add(future);
        } finally {
            lock.unlock();
        }

        future.get(timeout, unit);

    }

    @Override
    public void await() throws InterruptedException, CancellationException, ExecutionException {
        CompletableFuture local = tl.get();
        if (local != null) {

            tl.set(null);
            local.get();
            return;
        }
        CompletableFuture future = new CompletableFuture();
        lock.lock();
        try {
            futures.add(future);
        } finally {
            lock.unlock();
        }
        future.get();
    }

    public AwaiterTime singleUse() {
        ThreadLocal<Boolean> used = ThreadLocal.withInitial(() -> false);
        SnapshottingAwaiter me = this;
        return (long timeout, TimeUnit unit) -> {
            if (used.get()) {
                throw new IllegalStateException("Awaiter has been used on this thread");
            }
            used.set(true);
            try {
                me.await(timeout, unit);
            } catch (InterruptedException | ExecutionException | TimeoutException ex) {
                return false;
            }
            return true;
        };
    }

    public boolean hasWaiters() {
        lock.lock();
        try {
            return !futures.isEmpty();
        } finally {
            lock.unlock();
        }
    }

    public void prep() {
        CompletableFuture local = tl.get();
        if (local == null || local.isDone()) {
            CompletableFuture future = new CompletableFuture();

            lock.lock();
            try {
                futures.add(future);
            } finally {
                lock.unlock();
            }
            tl.set(future);
        }
    }

    public void completeAndReset() {
        lock.lock();
        try {
            Iterator<CompletableFuture> iter = futures.iterator();
            while (iter.hasNext()) {
                CompletableFuture next = iter.next();
                if (next != null) {
                    next.complete(0);
                    iter.remove();
                }
            }
        } finally {
            lock.unlock();
        }

    }

}
