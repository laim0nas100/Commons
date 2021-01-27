package lt.lb.commons.threads;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import lt.lb.commons.func.unchecked.UnsafeSupplier;

/**
 *
 * @author laim0nas100
 */
public abstract class AsyncUtil {

    public static interface AsyncTokenSupport {

        public boolean getToken() throws InterruptedException, TimeoutException;

        public boolean returnToken() throws InterruptedException, TimeoutException;

        public default void returnTokenOrThrow() throws InterruptedException, TimeoutException {
            if (!returnToken()) {
                throw new IllegalStateException("Failed to return the wait token");
            }
        }
    }

    /**
     * Unify get() and get(time,timeunit) methods with one interface
     *
     * @param <T>
     * @param compl where to save
     * @param poller how to await your turn to retrieve
     * @param getter how to retrieve
     * @return
     * @throws InterruptedException
     * @throws ExecutionException
     * @throws TimeoutException
     */
    public static <T> T waitedRetrieve(CompletableFuture<T> compl, AsyncTokenSupport poller, UnsafeSupplier<T> getter) throws InterruptedException, ExecutionException, TimeoutException {
        if (compl.isDone()) { //allready mapped, just get
            return compl.get();
        }
        boolean awaited = false;
        try {
            awaited = poller.getToken();
        } catch (InterruptedException | TimeoutException th) {
            throw th;
        }
        if (!awaited) {
            throw new IllegalStateException("No wait token but no exception either, error");
        }
        // we are mapping
        if (compl.isDone()) { //allready mapped while we were waiting, just get
            poller.returnTokenOrThrow();
            return compl.get();
        }
        try {
            T value = getter.unsafeGet();
            compl.complete(value);

        } catch (TimeoutException | InterruptedException ex) {
            poller.returnTokenOrThrow();
            throw ex;// non execution related exception, just pass is through and give a chance another thread to map.
        } catch (Throwable ex) { // execution related exception
            compl.completeExceptionally(ex);
        }
        poller.returnTokenOrThrow();
        return compl.get();
    }

    /**
     * Update atomic reference, atomically. If current value is null, the use
     * the creator {@link Supplier}, else use the updater {@link Function}. If
     * setting the reference was successful, then do nothing and return
     * {@code true}, else invoke updateCleanup {@link Consumer} with the newly
     * created value that was not set and return {@code false}.
     *
     * @param <T>
     * @param reference
     * @param creator
     * @param updater
     * @param updateCleanup
     * @return
     */
    public static <T> boolean atomicUpdate(AtomicReference<T> reference, Supplier<T> creator, Function<T, T> updater, Consumer<T> updateCleanup) {
        T oldRef = reference.get();
        if (oldRef == null) {
            T newRef = creator.get();
            if (reference.compareAndSet(oldRef, newRef)) {
                return true;
            } else {
                updateCleanup.accept(newRef);
                return false;
            }
        } else {
            T newRef = updater.apply(oldRef);
            if (reference.compareAndSet(oldRef, newRef)) {
                return true;
            } else {
                updateCleanup.accept(newRef);
                return false;
            }

        }
    }

    @FunctionalInterface
    public static interface IOSupplier<T> {

        public T get() throws IOException;
    }

    @FunctionalInterface
    public static interface IOFunction<T> {

        public T apply(T src) throws IOException;
    }

    /**
     * Update atomic reference, atomically. If current value is null, the use
     * the creator {@link IOSupplier}, else use the updater {@link IOFunction}.
     * If setting the reference was successful, then do nothing and return
     * {@code true}, otherwise invoke {@link Closeable#close()} with the newly
     * created value that was not set and return {@code false}.
     *
     * @param <T>
     * @param reference
     * @param creator
     * @param updater
     * @return
     * @throws java.io.IOException
     */
    public static <T extends Closeable> boolean atomicUpdateIO(AtomicReference<T> reference, IOSupplier<T> creator, IOFunction<T> updater) throws IOException {
        T oldRef = reference.get();
        if (oldRef == null) {
            T newRef = creator.get();
            if (reference.compareAndSet(oldRef, newRef)) {
                return true;
            } else {
                newRef.close();
                return false;
            }
        } else {
            T newRef = updater.apply(oldRef);
            if (reference.compareAndSet(oldRef, newRef)) {
                return true;
            } else {
                newRef.close();
                return false;
            }

        }
    }

}
