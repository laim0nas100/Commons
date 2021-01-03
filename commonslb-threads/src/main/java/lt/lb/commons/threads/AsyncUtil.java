package lt.lb.commons.threads;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
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
}
