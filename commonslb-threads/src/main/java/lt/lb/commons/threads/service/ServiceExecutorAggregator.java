package lt.lb.commons.threads.service;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import lt.lb.commons.threads.sync.Awaiter;

/**
 *
 * @author laim0nas100
 */
public interface ServiceExecutorAggregator extends ExecutorService {

    Collection<ExecutorService> getServices();

    default ExecutorService getMain() {
        return getServices().stream().findFirst().orElseThrow(() -> new IllegalStateException("No Executor services are configured"));
    }

    default <T> List<T> forEachCall(Function<ExecutorService, T> serv) {
        return getServices().stream().map(serv).collect(Collectors.toList());
    }

    default <T> List<T> forEachCallList(Function<ExecutorService, List<T>> serv) {
        return getServices().stream().map(serv).flatMap(m -> m.stream()).collect(Collectors.toList());
    }

    default boolean forEachCallAny(Function<ExecutorService, Boolean> serv) {
        return getServices().stream().map(serv).anyMatch(p -> p);
    }

    default boolean forEachCallAll(Function<ExecutorService, Boolean> serv) {
        return getServices().stream().map(serv).allMatch(p -> p);
    }

    default void forEach(Consumer<ExecutorService> serv) {
        getServices().forEach(serv);
    }

    @Override
    default void shutdown() {
        forEach(serv -> serv.shutdown());
    }

    @Override
    default List<Runnable> shutdownNow() {
        return forEachCallList(serv -> serv.shutdownNow());
    }

    @Override
    public default boolean isShutdown() {
        return forEachCallAll(serv -> serv.isShutdown());
    }

    @Override
    public default boolean isTerminated() {
        return forEachCallAll(serv -> serv.isTerminated());
    }

    @Override
    public default boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        List<Awaiter.AwaiterTime> collect = getServices().stream()
                .map(m -> new Awaiter.AwaiterTime() {
                    @Override
                    public boolean awaitBool(long timeout, TimeUnit unit) throws InterruptedException {
                        return m.awaitTermination(timeout, unit);
                    }
                })
                .collect(Collectors.toList());
        return Awaiter.sharedAwaitTimeBool(collect, timeout, unit);
    }

    @Override
    public default <T> Future<T> submit(Callable<T> task) {
        return getMain().submit(task);
    }

    @Override
    public default <T> Future<T> submit(Runnable task, T result) {
        return getMain().submit(task, result);
    }

    @Override
    public default Future<?> submit(Runnable task) {
        return getMain().submit(task);
    }

    @Override
    public default <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException {
        return getMain().invokeAll(tasks);
    }

    @Override
    public default <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException {
        return getMain().invokeAll(tasks, timeout, unit);
    }

    @Override
    public default <T> T invokeAny(Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException {
        return getMain().invokeAny(tasks);
    }

    @Override
    public default <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        return getMain().invokeAny(tasks, timeout, unit);
    }

    @Override
    public default void execute(Runnable command) {
        getMain().execute(command);
    }

}
