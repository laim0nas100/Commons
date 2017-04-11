/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package LibraryLB.Threads;

import LibraryLB.Log;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.TimeUnit;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.IntegerBinding;
import javafx.collections.FXCollections;

/**
 *
 * @author Lemmin
 */
public class DynamicTaskExecutor extends AbstractExecutorService{
    protected TaskProvider provider = new TaskProvider();
    protected ConcurrentLinkedDeque<TaskRunner> activeRunners = new ConcurrentLinkedDeque<>();
    protected ConcurrentLinkedDeque<TaskRunner> disabledRunners = new ConcurrentLinkedDeque<>();
    protected BigInteger tasksFinished = BigInteger.ZERO;
    protected BigInteger tasksAdded = BigInteger.ZERO;
    protected Runnable doOnFinish = () -> increment();
    protected int activeCount = 0;
    protected boolean shutdownCalled = false;
    protected ExtTask terminationTask = new ExtTask(-1) {
        @Override
        protected Object call() throws Exception {
            List<TaskRunner> list = new ArrayList<>();
            list.addAll(disabledRunners);
            list.addAll(activeRunners);
            Log.print("Termination task running");
            for(TaskRunner runner:list){
                runner.wakeUp();
                runner.me.join();
                Log.print("Joined!!!");
            }
            Log.print("Termination task finished");
            return 0;
        }
    };
    public void wakeUpRunners(){      
        for(TaskRunner runner:activeRunners){
            runner.wakeUp();
        }
    }
    protected synchronized void increment(){
        tasksFinished.add(BigInteger.ONE);
    }
    protected TaskRunner createRunner(Runnable doOnFinish){      
        TaskRunner runner = null;
        TaskRunner tryMe = disabledRunners.pollLast();
        if(tryMe == null || tryMe.me.isAlive()){
            if(tryMe != null && tryMe.me.isAlive()){
                disabledRunners.addFirst(tryMe);
            }
        }else{
            runner = tryMe;
        }
        if(runner == null){
            runner = new DynamicTaskRunner(provider,doOnFinish);   
        }
        activeRunners.add(runner);
        Thread t = new Thread(runner);
        runner.me = t;
        return runner;
    }
    public void setRunnerSize(int size){
        activeCount = Math.max(size, 0);
        while(activeRunners.size() <  activeCount){
            createRunner(this.doOnFinish).me.start();
        }
        while(activeRunners.size() > activeCount){
            TaskRunner runner = activeRunners.pollLast();
            runner.disable();
            this.disabledRunners.add(runner);
        }
        Log.print("New runner size",this.activeCount);
    }
    
    public void cancelAllTasks(){
        
        for(TaskRunner runner:this.activeRunners){
            if(runner.task != null){
                runner.task.cancel(true);
            }
        }
        for(RunnableFuture task:provider.tasks){
            task.cancel(true);
        }
    }
    public void stopEverything(){
        clearPendingTasks();
        cancelAllTasks();

    }
    public void stopEverythingStartThis(Runnable runnable){
        stopEverything();
        provider.submit(runnable, true);
        wakeUpRunners();
    }
    public void clearPendingTasks(){
        provider.tasks.clear();
    }

    @Override
    public void shutdown() {      
        Log.print("Shutdown call");
        if(!shutdownCalled){
            Log.print("Shutdown commance");
            Runnable checkEmpty = () ->{
                if(provider.tasks.isEmpty()){
                    setRunnerSize(0);
                }
                Log.print("Update size",provider.tasks.size());
            };
            checkEmpty.run();
            provider.onTaskRequest = checkEmpty;
            shutdownCalled = true;
            Log.print("start task");
            terminationTask.toThread().start();
        }
    }

    @Override
    public List<Runnable> shutdownNow() {
        shutdown();
        List<Runnable> list = Collections.emptyList();
        Runnable run = provider.tasks.pollFirst();
        while(run != null){
            list.add(run);
            run = provider.tasks.pollFirst();
        }
        return list;
    }

    @Override
    public boolean isShutdown() {
        return this.activeCount == 0;
    }

    @Override
    public boolean isTerminated() {
        boolean allDead = isShutdown();
        Iterator<TaskRunner> iterator = this.disabledRunners.iterator();
        while(allDead){
            if(iterator.hasNext()){
                allDead = !(iterator.next().me.isAlive());
            }
        }
        return allDead;
    }

    @Override
    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        Object get = terminationTask.get(timeout, unit);
        return get != null;
    
    }

    @Override
    public synchronized void execute(Runnable command) {
        if(!shutdownCalled){
            provider.submit(command,false);
            this.tasksAdded.add(BigInteger.ONE);
        }
        wakeUpRunners();
    }
    
}
