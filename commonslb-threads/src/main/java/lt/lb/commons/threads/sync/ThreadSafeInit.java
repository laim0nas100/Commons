package lt.lb.commons.threads.sync;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;
import lt.lb.commons.misc.NestedException;

/**
 *
 * @author laim0nas100
 */
public class ThreadSafeInit<T> {

    private AtomicBoolean inAssign = new AtomicBoolean(false);
    private volatile T value;
    private volatile boolean initialized = false;
    private CompletableFuture<T> future = new CompletableFuture<>();

    public boolean tryInit(Supplier<T> supplier) {
        if (inAssign.compareAndSet(false, true)) {
            value = supplier.get();
            future.complete(value);
            initialized = true;
        }
        if (!initialized) {
            inAssign.set(false);
        }
        return initialized;
    }
    
    public T tryInitOrGet(Supplier<T> supplier) throws InterruptedException, ExecutionException{
        tryInit(supplier);
        return get();
        
    }
    
    public T tryInitOrGetUnsafe(Supplier<T> supplier){
        tryInit(supplier);
        return getUnsafe();
        
    }

    public T get() throws InterruptedException, ExecutionException {
        if (initialized) {
            return value;
        }
        return future.get();
    }

    public T getUnsafe() {
        if (initialized) {
            return value;
        }
        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            throw NestedException.of(e);
        }
    }

}
