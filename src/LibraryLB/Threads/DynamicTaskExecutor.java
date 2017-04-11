/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package LibraryLB.Threads;

import LibraryLB.Log;
import java.math.BigInteger;
import java.util.List;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author Lemmin
 */
public class DynamicTaskExecutor extends AbstractExecutorService{
    protected TaskProvider provider = new TaskProvider();
    protected ConcurrentLinkedDeque<TaskRunner> runners = new ConcurrentLinkedDeque<>();
    protected BigInteger tasksFinished = BigInteger.ZERO;
    protected BigInteger tasksAdded = BigInteger.ZERO;
    protected Runnable doOnFinish = () -> increment();
    protected int activeCount = 1;
//    public void submit(Callable cal){
//        provider.submit(cal,false);
//        wakeUpRunners();
//    }
//    public void submit(Runnable run){
//        provider.submit(run,false);
//        wakeUpRunners();
//    }
    public void wakeUpRunners(){
        
        for(TaskRunner runner:runners){
            runner.wakeUp();
        }
    }
    protected synchronized void increment(){
        tasksFinished.add(BigInteger.ONE);
    }
    protected TaskRunner createRunner(Runnable doOnFinish){      
        TaskRunner runner = new DynamicTaskRunner(provider,doOnFinish);
        runners.add(runner);
        Thread t = new Thread(runner);
        runner.me = t;
        return runner;
    }
    public void setRunnerSize(int size){
        activeCount = Math.max(size, 0);
        while(runners.size() <  activeCount){
            createRunner(this.doOnFinish).me.start();
        }
        while(runners.size() > activeCount){
            runners.pollLast().disable();
        }
        Log.print("New runner size",this.activeCount);
    }
    
    public void cancelAllTasks(){
        
        for(TaskRunner runner:this.runners){
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
        setRunnerSize(0);
    }

    @Override
    public List<Runnable> shutdownNow() {
        return null;
    }

    @Override
    public boolean isShutdown() {
        return false;
    }

    @Override
    public boolean isTerminated() {
        return false;
    }

    @Override
    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        return false;
    
    }

    @Override
    public synchronized void execute(Runnable command) {
        provider.submit(command,false);
        this.tasksAdded.add(BigInteger.ONE);
        wakeUpRunners();
    }
    
}
