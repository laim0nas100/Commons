package lt.lb.commons.threads.service;

import java.util.Collection;
import java.util.Collections;
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
import java.util.function.Supplier;

/**
 *
 * @author laim0nas100
 */
public class ServiceExecutorAggregatorBase extends AbstractExecutorService implements ScheduledExecutorService, ServiceExecutorAggregator {

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

    protected ExecutorService getOrCreate(String name, Supplier<? extends ExecutorService> supplier) {
        return servMap.compute(name, (k, current) -> {
            if(current == null){
                if(isShutdown()){
                    throw new IllegalStateException("Shutdown has been called, cannot create new service by name:"+name);
                }
                return serviceSupplier.getOrDefault(k, supplier).get();
            }
            return current;
        });
    }

    public ExecutorService service(String servName) {
        return getOrCreate(servName, defaultSupplier);
    }

    public ScheduledExecutorService scheduledService(String servName) {
        ExecutorService service = getOrCreate(servName, defaultSchedulerSupplier);
        if (service instanceof ScheduledExecutorService) {
            return (ScheduledExecutorService) service;
        }
        throw new IllegalArgumentException("Requested service is allready registered, and it's not of type:" + ScheduledExecutorService.class + ", it's" + service);
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
    public ExecutorService getMain() {
        return service(mainServiceName);
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

    @Override
    public Collection<ExecutorService> getServices() {
        return Collections.unmodifiableCollection(servMap.values());
    }

}
