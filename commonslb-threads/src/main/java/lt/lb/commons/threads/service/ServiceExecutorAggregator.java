package lt.lb.commons.threads.service;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 *
 * @author laim0nas100
 */
public class ServiceExecutorAggregator {

    protected ConcurrentHashMap<String, ExecutorService> servMap = new ConcurrentHashMap<>();
    protected Supplier<ExecutorService> defaultSupplier = () -> Executors.newSingleThreadExecutor();
    protected Supplier<ScheduledExecutorService> defaultSchedulerSupplier = () -> Executors.newSingleThreadScheduledExecutor();
    protected HashMap<String, Supplier<? extends ExecutorService>> serviceSupplier = new HashMap<>();
    protected volatile boolean shutdown;

    public void setService(String name, final int threads) {
        setService(name, () -> Executors.newFixedThreadPool(threads));
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
        setService(name, () -> Executors.newScheduledThreadPool(threads));
    }

    public boolean containsService(String name) {
        return servMap.containsKey(name);
    }

    public ExecutorService service(String servName) {
        return servMap.compute(servName, (k, old) -> {
            if (old == null) {
                return serviceSupplier.getOrDefault(servName, defaultSupplier).get();
            } else {
                return old;
            }
        });
    }

    public ScheduledExecutorService scheduledService(String servName) {
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
        return servMap.values().stream().map(serv).filter(f -> f).findAny().isPresent();
    }

    void forEach(Consumer<ExecutorService> serv) {
        servMap.values().stream().forEach(serv);
    }

    void shutdown() {
        shutdown = true;
        forEach(serv -> serv.shutdown());
    }

    List<Runnable> shutdownNow() {
        shutdown = true;
        return forEachCallList(serv -> serv.shutdownNow());
    }

    boolean isShutdown() {
        return shutdown;
    }

    boolean isTerminated() {
        return forEachCallAny(serv -> serv.isTerminated());
    }

}
