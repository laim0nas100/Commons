/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package LibraryLB.Threads;

import LibraryLB.Log;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import javafx.beans.binding.BooleanExpression;
import javafx.beans.property.SimpleBooleanProperty;

/**
 *
 * @author Lemmin
 */
public class TimeoutTask implements ScheduledExecutorService{
    private final ScheduledExecutorService executor;
    private final long timeout;
    private volatile long currentTime;
    private boolean initiated;
    private BooleanExpression conditionalCheck;
    private ArrayList<Runnable> onUpdate = new ArrayList<>();
    
    /**
     *
     * @param timeout Milliseconds to wait until execution
     * @param refreshRate  Timer update rate (Milliseconds)
     * @param run   Task to execute after timer reaches zero
     */
    public TimeoutTask(long timeout,long refreshRate,Runnable run){
        this.timeout = timeout;
        initiated = false;
        conditionalCheck = new SimpleBooleanProperty(true);
        executor = Executors.newScheduledThreadPool(1);
        executor.scheduleAtFixedRate(()->{
            if(initiated){
                this.currentTime-= refreshRate;
                if(conditionalChecksSatisfied() && this.currentTime<=0){
                    initiated = false;
                    run.run();
                }
                currentTime = Math.max(0, currentTime);
            }
        }, 0, refreshRate, TimeUnit.MILLISECONDS);
        
    }

    /**
     *  start timer or update timer if already started
     */
    public void update(){
       for(Runnable run:this.onUpdate){
           run.run();
       }
       currentTime = timeout;
       initiated = true;
       
    }
    public boolean isInAction(){
        return this.initiated;
    }
    public void addOnUpdate(Runnable run){
        this.onUpdate.add(run);
    }
    
    public void andConditionalCheck(BooleanExpression prop){
        conditionalCheck = conditionalCheck.and(prop);
    }
    public void orConditionalCheck(BooleanExpression prop){
        conditionalCheck = conditionalCheck.and(prop);
    }
    public boolean conditionalChecksSatisfied(){
        return conditionalCheck.get();
    }

    @Override
    public void execute(Runnable r) {
        this.executor.execute(r);
    }

    @Override
    public void shutdown() {
        this.executor.shutdown();
    }

    @Override
    public List<Runnable> shutdownNow() {
       return this.executor.shutdownNow();
    }

    @Override
    public boolean isShutdown() {
        return this.executor.isShutdown();
    }

    @Override
    public boolean isTerminated() {
        return this.executor.isTerminated();
    }

    @Override
    public boolean awaitTermination(long l, TimeUnit tu) throws InterruptedException {
        return this.executor.awaitTermination(l, tu);
    }

    @Override
    public ScheduledFuture<?> schedule(Runnable r, long l, TimeUnit tu) {
        return this.executor.schedule(r, l, tu);
    }

    @Override
    public <V> ScheduledFuture<V> schedule(Callable<V> clbl, long l, TimeUnit tu) {
        return this.executor.schedule(clbl, l, tu);
    }

    @Override
    public ScheduledFuture<?> scheduleAtFixedRate(Runnable r, long l, long l1, TimeUnit tu) {
        return this.executor.scheduleAtFixedRate(r, l, l1, tu);
    }

    @Override
    public ScheduledFuture<?> scheduleWithFixedDelay(Runnable r, long l, long l1, TimeUnit tu) {
        return this.executor.scheduleWithFixedDelay(r, l, l1, tu);
    }

    @Override
    public <T> Future<T> submit(Callable<T> clbl) {
        return this.executor.submit(clbl);
    }

    @Override
    public <T> Future<T> submit(Runnable r, T t) {
        return this.executor.submit(r, t);
    }

    @Override
    public Future<?> submit(Runnable r) {
        return this.executor.submit(r);
    }

    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> clctn) throws InterruptedException {
        return this.executor.invokeAll(clctn);
    }

    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> clctn, long l, TimeUnit tu) throws InterruptedException {
        return this.executor.invokeAll(clctn, l, tu);
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> clctn) throws InterruptedException, ExecutionException {
        return this.executor.invokeAny(clctn);
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> clctn, long l, TimeUnit tu) throws InterruptedException, ExecutionException, TimeoutException {
        return this.executor.invokeAny(clctn, l, tu);
    }

}
