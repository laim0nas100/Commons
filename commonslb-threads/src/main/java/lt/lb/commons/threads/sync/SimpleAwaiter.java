package lt.lb.commons.threads.sync;

import java.util.Iterator;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 *
 * @author laim0nas100
 */
public class SimpleAwaiter implements Awaiter.AwaiterTime {

    private LinkedBlockingDeque<CompletableFuture> futures = new LinkedBlockingDeque<>();

    private ThreadLocal<CompletableFuture> tl = ThreadLocal.withInitial(() -> null);

    @Override
    public void await(long timeout, TimeUnit unit) throws InterruptedException, TimeoutException {
        try {
            CompletableFuture local = tl.get();
            if (local != null) {

                tl.set(null);
                local.get(timeout, unit);
                return;
            }
            CompletableFuture future = new CompletableFuture();
            futures.add(future);
            future.get(timeout, unit);
        } catch (ExecutionException ex) {

        }

    }

    public AwaiterTime singleUse() {
        ThreadLocal<Boolean> used = ThreadLocal.withInitial(() -> false);
        SimpleAwaiter me = this;
        return (long timeout, TimeUnit unit) -> {
            if (used.get()) {
                throw new IllegalStateException("Awaiter has been used on this thread");
            }
            used.set(true);
            me.await(timeout, unit);
        };
    }

    public boolean hasWaiters() {
        return !futures.isEmpty();
    }

    public void prep() {
        if (tl.get() == null) {
            CompletableFuture future = new CompletableFuture();
            futures.add(future);
            tl.set(future);
        }
    }

    public void completeAndReset() {
        Iterator<CompletableFuture> iter = futures.iterator();
        while (iter.hasNext()) {
            CompletableFuture next = iter.next();
            if (next != null) {
                next.complete(0);
                iter.remove();
            }
        }
    }

}
