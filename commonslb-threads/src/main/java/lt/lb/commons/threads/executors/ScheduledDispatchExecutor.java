package lt.lb.commons.threads.executors;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import lt.lb.commons.containers.values.BooleanValue;

/**
 *
 * @author laim0nas100
 */
public class ScheduledDispatchExecutor {

    protected ScheduledExecutorService dispatcher;
    protected Map<String, BooleanValue> enabledMap = new ConcurrentHashMap<>();

    public ScheduledDispatchExecutor() {
    }

    public String addSchedulingTask(Executor exe, Runnable call, TimeUnit tu, long dur) {
        return this.addSchedulingTask(exe, call, tu, dur, dur);
    }
    
    public String addSchedulingTask(Executor exe, Runnable call, TimeUnit tu, long initialDelay, long period) {
        BooleanValue enabled = new BooleanValue(true);
        Runnable runProxy = () -> {
            if (enabled.get()) {
                exe.execute(call);
            }
        };
        String nextUUID = UUID.randomUUID().toString();
        while (enabledMap.containsKey(nextUUID)) {
            nextUUID = UUID.randomUUID().toString();
        }
        enabledMap.put(nextUUID, enabled);
        getDispatcher().scheduleAtFixedRate(runProxy, initialDelay, period, tu);
        return nextUUID;
    }

    protected ScheduledExecutorService getDispatcher() {
        if (dispatcher == null) {
            dispatcher = Executors.newSingleThreadScheduledExecutor();
        }
        return dispatcher;
    }

    public void shutdown() {
        this.getDispatcher().shutdown();
        this.dispatcher = null;
    }

    public void setEnabledTask(String uuid, boolean enable) {
        if (this.enabledMap.containsKey(uuid)) {
            this.enabledMap.get(uuid).set(enable);
        }
    }

    public void setEnabledAll(boolean enabled) {
        this.enabledMap.values().forEach(value -> {
            value.set(enabled);
        });
    }

}
