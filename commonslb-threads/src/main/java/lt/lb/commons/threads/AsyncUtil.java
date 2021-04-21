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
import lt.lb.uncheckedutils.func.UncheckedSupplier;

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

        /**
         * Unify get() and get(time,timeunit) methods with one interface
         *
         * @param <T>
         * @param compl where to save
         * @param getter how to retrieve
         * @return
         * @throws InterruptedException
         * @throws ExecutionException
         * @throws TimeoutException
         */
        public default <T> T waitedRetrieve(CompletableFuture<T> compl, UncheckedSupplier<T> getter) throws InterruptedException, ExecutionException, TimeoutException {
            if (compl.isDone()) { //allready mapped, just get
                return compl.get();
            }
            boolean awaited = false;
            try {
                awaited = this.getToken();
            } catch (InterruptedException | TimeoutException th) {
                throw th;
            }
            if (!awaited) {
                throw new IllegalStateException("No wait token but no exception either, error");
            }
            // we are mapping
            if (compl.isDone()) { //allready mapped while we were waiting, just get
                this.returnTokenOrThrow();
                return compl.get();
            }
            try {
                T value = getter.uncheckedGet();
                compl.complete(value);

            } catch (TimeoutException | InterruptedException ex) {
                this.returnTokenOrThrow();
                throw ex;// non execution related exception, just pass is through and give a chance another thread to map.
            } catch (Throwable ex) { // execution related exception
                compl.completeExceptionally(ex);
            }
            this.returnTokenOrThrow();
            return compl.get();
        }
    }

    /**
     * Update atomic reference, atomically. If current value is null, the use
     * the creator {@link Supplier}, else use the updater {@link Function}. If
     * setting the reference was successful and the newly set reference is
     * different that previous, then do nothing and return {@code true}, else
     * invoke updateCleanup {@link Consumer} with the newly created value that
     * was not set and return {@code false}.
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
                return newRef != oldRef;
            } else {
                updateCleanup.accept(newRef);
                return false;
            }
        } else {
            T newRef = updater.apply(oldRef);
            if (reference.compareAndSet(oldRef, newRef)) {
                return newRef != oldRef;
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
     * If setting the reference was successful and the newly set reference is
     * different that previous, then do nothing and return {@code true},
     * otherwise invoke {@link Closeable#close()} with the newly created value
     * that was not set and return {@code false}.
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
                return oldRef != newRef;
            } else {
                newRef.close();
                return false;
            }
        } else {
            T newRef = updater.apply(oldRef);
            if (reference.compareAndSet(oldRef, newRef)) {
                return oldRef != newRef;
            } else {
                newRef.close();
                return false;
            }

        }
    }

}
