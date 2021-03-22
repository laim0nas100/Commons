package lt.lb.commons.threads.service;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import lt.lb.commons.threads.sync.Awaiter;

/**
 *
 * @author laim0nas100
 */
public class ServiceExecutorAggregator extends AbstractExecutorService implements ScheduledExecutorService {

    protected ConcurrentHashMap<String, ExecutorService> servMap = new ConcurrentHashMap<>();
    protected Supplier<ExecutorService> defaultSupplier = () -> createExecutor(1);
    protected Supplier<ScheduledExecutorService> defaultSchedulerSupplier = () -> createScheduledExecutor(1);
    protected HashMap<String, Supplier<? extends ExecutorService>> serviceSupplier = new HashMap<>();
    protected volatile boolean shutdown;
    protected volatile String mainServiceName = "main";
    protected volatile String mainSchedulerServiceName = "main-scheduler";

    public void setService(String name, final int threads) {
        setService(name, () -> createExecutor(threads));
    }

    protected ExecutorService createExecutor(int threads) {
        return Executors.newFixedThreadPool(threads);
    }

    protected ScheduledExecutorService createScheduledExecutor(int threads) {
        return Executors.newScheduledThreadPool(threads);
    }

    public void setService(String name, Supplier<? extends ExecutorService> serviceSupl) {
        Objects.requireNonNull(serviceSupl);
        Objects.requireNonNull(name);
        serviceSupplier.put(name, serviceSupl);
    }

    public void setScheduledService(String name, Supplier<? extends ScheduledExecutorService> serviceSupl) {
        setService(name, serviceSupl);
    }

    public void setScheduledService(String name, final int threads) {
        setService(name, () -> createScheduledExecutor(threads));
    }

    public void setMainService(String name) {
        this.mainServiceName = Objects.requireNonNull(name);
    }

    public void setMainSchedulerService(String name) {
        this.mainSchedulerServiceName = Objects.requireNonNull(name);
    }

    public boolean containsService(String name) {
        return servMap.containsKey(name);
    }

    public ExecutorService service(String servName) {
        if (shutdown) {
            throw new IllegalStateException("Shutdown has been called");
        }
        return servMap.compute(servName, (k, old) -> {
            if (old == null) {
                return serviceSupplier.getOrDefault(servName, defaultSupplier).get();
            } else {
                return old;
            }
        });
    }

    public ScheduledExecutorService scheduledService(String servName) {
        if (shutdown) {
            throw new IllegalStateException("Shutdown has been called");
        }
        return (ScheduledExecutorService) servMap.compute(servName, (k, old) -> {
            if (old == null) {
                return serviceSupplier.getOrDefault(servName, defaultSchedulerSupplier).get();
            } else {
                if (old instanceof ScheduledExecutorService) {
                    return old;
                }
                throw new IllegalArgumentException("Requested service is allready registered, and it's not of type:ScheduledExecutorService, it's" + old);
            }
        });
    }

    <T> List<T> forEachCall(Function<ExecutorService, T> serv) {
        return servMap.values().stream().map(serv).collect(Collectors.toList());
    }

    <T> List<T> forEachCallList(Function<ExecutorService, List<T>> serv) {
        return servMap.values().stream().map(serv).flatMap(m -> m.stream()).collect(Collectors.toList());
    }

    boolean forEachCallAny(Function<ExecutorService, Boolean> serv) {
        return servMap.values().stream().map(serv).anyMatch(p -> p);
    }

    boolean forEachCallAll(Function<ExecutorService, Boolean> serv) {
        return servMap.values().stream().map(serv).allMatch(p -> p);
    }

    void forEach(Consumer<ExecutorService> serv) {
        servMap.values().stream().forEach(serv);
    }

    @Override
    public void shutdown() {
        shutdown = true;
        forEach(serv -> serv.shutdown());
    }

    @Override
    public List<Runnable> shutdownNow() {
        shutdown = true;
        return forEachCallList(serv -> serv.shutdownNow());
    }

    @Override
    public boolean isShutdown() {
        return shutdown;
    }

    @Override
    public boolean isTerminated() {
        return forEachCallAll(serv -> serv.isTerminated());
    }

    @Override
    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        List<Awaiter.AwaiterTime> collect = servMap.values().stream()
                .map(m -> Awaiter.fromFunction(m::awaitTermination)).collect(Collectors.toList());
        return Awaiter.sharedAwaitTimeBool(collect, timeout, unit);
    }

    @Override
    public void execute(Runnable command) {
        service(mainServiceName).execute(command);
    }

    @Override
    public ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit) {
        return scheduledService(mainSchedulerServiceName).schedule(command, delay, unit);
    }

    @Override
    public <V> ScheduledFuture<V> schedule(Callable<V> callable, long delay, TimeUnit unit) {
        return scheduledService(mainSchedulerServiceName).schedule(callable, delay, unit);
    }

    @Override
    public ScheduledFuture<?> scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit) {
        return scheduledService(mainSchedulerServiceName).scheduleAtFixedRate(command, initialDelay, period, unit);
    }

    @Override
    public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command, long initialDelay, long delay, TimeUnit unit) {
        return scheduledService(mainSchedulerServiceName).scheduleWithFixedDelay(command, initialDelay, delay, unit);
    }

}
