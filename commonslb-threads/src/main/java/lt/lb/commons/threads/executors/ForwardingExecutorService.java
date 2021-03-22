package lt.lb.commons.threads.executors;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 *
 * @author laim0nas100
 */
public interface ForwardingExecutorService extends ExecutorService {

    public ExecutorService delegate();

    @Override
    public default void shutdown() {
        delegate().shutdown();
    }

    @Override
    public default List<Runnable> shutdownNow() {
        return delegate().shutdownNow();
    }

    @Override
    public default boolean isShutdown() {
        return delegate().isShutdown();
    }

    @Override
    public default boolean isTerminated() {
        return delegate().isTerminated();
    }

    @Override
    public default boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        return delegate().awaitTermination(timeout, unit);
    }

    @Override
    public default <T> Future<T> submit(Callable<T> task) {
        return delegate().submit(task);
    }

    @Override
    public default <T> Future<T> submit(Runnable task, T result) {
        return delegate().submit(task, result);
    }

    @Override
    public default Future<?> submit(Runnable task) {
        return delegate().submit(task);
    }

    @Override
    public default <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException {
        return delegate().invokeAll(tasks);
    }

    @Override
    public default <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException {
        return delegate().invokeAll(tasks, timeout, unit);
    }

    @Override
    public default <T> T invokeAny(Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException {
        return delegate().invokeAny(tasks);
    }

    @Override
    public default <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        return delegate().invokeAny(tasks, timeout, unit);
    }

    @Override
    public default void execute(Runnable command) {
        delegate().execute(command);
    }

}
