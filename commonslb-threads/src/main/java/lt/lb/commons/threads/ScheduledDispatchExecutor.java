/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lt.lb.commons.threads;

import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 *
 * @author Laimonas-Beniusis-PC
 */
public class ScheduledDispatchExecutor {
    

    protected ScheduledExecutorService dispatcher;
    protected HashMap<String, AtomicBoolean> enabledMap = new HashMap<>();

    public ScheduledDispatchExecutor() {
    }

    public String addSchedulingTask(Executor exe, Runnable call, TimeUnit tu, long dur) {
        AtomicBoolean enabled = new AtomicBoolean(true);
        Runnable runProxy = () -> {
            if (enabled.get()) {
                exe.execute(call);
            } 
        };
        String nextUUID = UUID.randomUUID().toString();
        while(enabledMap.containsKey(nextUUID)){
            nextUUID = UUID.randomUUID().toString();
        }
        enabledMap.put(nextUUID, enabled);
        getDispatcher().scheduleAtFixedRate(runProxy, dur, dur, tu);
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
    
    public void setEnabledAll(boolean enabled){
        this.enabledMap.values().forEach(value ->{
            value.set(enabled);
        });
    }

    
}