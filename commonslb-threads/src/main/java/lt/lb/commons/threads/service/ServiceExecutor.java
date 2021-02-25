package lt.lb.commons.threads.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Delayed;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 *
 * Service to delegate scheduling to one executor, and the execution to other
 * executor, while combining the {@link ScheduledFuture} into one.
 *
 * @author laim0nas100
 */
public class ServiceExecutor implements ScheduledExecutorService {

    protected ExecutorService pool;
    protected ScheduledExecutorService service;

    public ServiceExecutor() {
        this(ForkJoinPool.commonPool(), Executors.newSingleThreadScheduledExecutor());
    }

    public ServiceExecutor(ExecutorService service) {
        this(Objects.requireNonNull(service), Executors.newSingleThreadScheduledExecutor());
    }

    public ServiceExecutor(int threads) {
        this(Executors.newFixedThreadPool(Math.max(1, threads)), Executors.newSingleThreadScheduledExecutor());
    }

    public ServiceExecutor(ExecutorService pool, ScheduledExecutorService service) {
        this.pool = Objects.requireNonNull(pool);
        this.service = Objects.requireNonNull(service);
    }

    @Override
    public ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit) {
        return schedule(Executors.callable(command), delay, unit);
    }

    protected <V> ScheduledFuture<V> from(Callable<V> call, Function<FutureTask<V>, ScheduledFuture<Void>> scheduler) {
        final FutureTask<V> finalValue = new FutureTask<>(call);

        ScheduledFuture<Void> s = scheduler.apply(finalValue);

        return new ScheduledFuture<V>() {
            @Override
            public long getDelay(TimeUnit unit) {
                return s.getDelay(unit);
            }

            @Override
            public int compareTo(Delayed o) {
                return s.compareTo(o);
            }

            @Override
            public boolean cancel(boolean mayInterruptIfRunning) {
                boolean c1 = finalValue.cancel(mayInterruptIfRunning);
                boolean c2 = s.cancel(mayInterruptIfRunning);
                return c1 || c2;
            }

            @Override
            public boolean isCancelled() {
                return finalValue.isCancelled();
            }

            @Override
            public boolean isDone() {
                return finalValue.isDone();
            }

            @Override
            public V get() throws InterruptedException, ExecutionException {
                return finalValue.get();
            }

            @Override
            public V get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
                return finalValue.get(timeout, unit);
            }
        };
    }

    @Override
    public <V> ScheduledFuture<V> schedule(Callable<V> callable, long delay, TimeUnit unit) {

        return from(callable, task -> {
            return service.schedule(() -> {
                pool.submit(task);
                return null;
            }, delay, unit);
        });
    }

    protected ScheduledFuture<?> fromRepeated(Runnable command, Function<Runnable, ScheduledFuture<?>> scheduler) {
        AtomicInteger ids = new AtomicInteger(0);
        Map<Integer, Future> values = new ConcurrentHashMap<>();
        final FutureTask<Void> firstValue = new FutureTask<>(Executors.callable(command, null));

        Runnable run = () -> {
            int id = ids.getAndIncrement();
            if (id == 0) {
                pool.submit(firstValue);
            } else {
                Future<?> submit = pool.submit(command);
                values.put(id, submit);
                Collection<Integer> keys = new ArrayList<>(values.keySet());
                for (Integer k : keys) {
                    if (k == null) {
                        continue;
                    }
                    values.computeIfPresent(k, (kk, old) -> {
                        if (old.isDone()) {
                            return null;
                        }
                        return old;
                    });
                }

            }
        };
        ScheduledFuture<?> s = scheduler.apply(run);

        return new ScheduledFuture<Void>() {
            @Override
            public long getDelay(TimeUnit unit) {
                return s.getDelay(unit);
            }

            protected Stream<Future> futures() {
                return values.values().stream().filter(f -> f != null);
            }

            @Override
            public int compareTo(Delayed o) {
                return s.compareTo(o);
            }

            @Override
            public boolean cancel(boolean mayInterruptIfRunning) {
                boolean c1 = firstValue.cancel(mayInterruptIfRunning);
                boolean c2 = s.cancel(mayInterruptIfRunning);

                boolean c3 = futures().map(m -> m.cancel(mayInterruptIfRunning)).filter(f -> f).findAny().isPresent();
                return c1 || c2 || c3;
            }

            @Override
            public boolean isCancelled() {
                return firstValue.isCancelled();
            }

            @Override
            public boolean isDone() {
                return firstValue.isDone();
            }

            @Override
            public Void get() throws InterruptedException, ExecutionException {
                return firstValue.get();
            }

            @Override
            public Void get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
                return firstValue.get(timeout, unit);
            }
        };
    }

    @Override
    public ScheduledFuture<?> scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit) {
        return fromRepeated(command, run -> {
            return service.scheduleAtFixedRate(run, initialDelay, period, unit);
        });
    }

    @Override
    public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command, long initialDelay, long delay, TimeUnit unit) {
        return fromRepeated(command, run -> {
            return service.scheduleWithFixedDelay(run, initialDelay, delay, unit);
        });
    }

    @Override
    public void shutdown() {
        pool.shutdown();
        service.shutdown();
    }

    @Override
    public List<Runnable> shutdownNow() {
        List<Runnable> shutdownNowPool = pool.shutdownNow();
        List<Runnable> shutdownNowScheduled = service.shutdownNow();
        ArrayList<Runnable> arrayList = new ArrayList<>(shutdownNowPool.size() + shutdownNowScheduled.size());

        arrayList.addAll(shutdownNowPool);
        arrayList.addAll(shutdownNowScheduled);
        return arrayList;
    }

    @Override
    public boolean isShutdown() {
        return pool.isShutdown();
    }

    @Override
    public boolean isTerminated() {
        return pool.isTerminated();
    }

    @Override
    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        return pool.awaitTermination(timeout, unit);
    }

    @Override
    public <T> Future<T> submit(Callable<T> task) {
        return pool.submit(task);
    }

    @Override
    public <T> Future<T> submit(Runnable task, T result) {
        return pool.submit(task, result);
    }

    @Override
    public Future<?> submit(Runnable task) {
        return pool.submit(task);
    }

    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException {
        return pool.invokeAll(tasks);
    }

    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException {
        return pool.invokeAll(tasks, timeout, unit);
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException {
        return pool.invokeAny(tasks);
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        return pool.invokeAny(tasks, timeout, unit);
    }

    @Override
    public void execute(Runnable command) {
        pool.execute(command);
    }

}
