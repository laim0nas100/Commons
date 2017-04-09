/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package LibraryLB.Threads;

import LibraryLB.Log;
import java.math.BigInteger;
import java.util.Iterator;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 * @author Lemmin
 */
public class DynamicTaskExecutor{
    public TaskProvider provider = new TaskProvider();
    public ConcurrentLinkedDeque<TaskRunner> runners = new ConcurrentLinkedDeque<>();
    public BigInteger tasksFinished = BigInteger.ZERO;
    public AtomicInteger sleepingCount = new AtomicInteger(0);
    
    public int activeCount = 1;
    public void submit(Callable cal){
        provider.submit(cal,false);
        wakeUpRunners();
    }
    public void submit(Runnable run){
        provider.submit(run,false);
        wakeUpRunners();
    }
    public void wakeUpRunners(){
        
        for(TaskRunner runner:runners){
            runner.wakeUp();
        }
        Iterator<RunnableFuture> iterator = provider.activeTasks.iterator();
        while(iterator.hasNext()){
            iterator.next().isDone();
            iterator.remove();
        }
    }
    private synchronized void increment(){
        tasksFinished.add(BigInteger.ONE);
    }
    public TaskRunner createRunner(){
        Runnable r = () ->{
            increment();
        };
        
        TaskRunner runner = new TaskRunner(this,r);
        runners.add(runner);
        return runner;
    }
    public void setRunnerSize(int size){
        activeCount = Math.max(size, 0);
        while(runners.size() <  activeCount){
                new Thread(new FutureTask(createRunner())).start(); 
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
        for(RunnableFuture task:provider.activeTasks){
            task.cancel(true);
        }
    }
    public void stopEverythingStartThis(Runnable runnable){
        stopEverything();
        provider.submit(runnable, true);
        wakeUpRunners();
    }
    public void clearPendingTasks(){
        provider.tasks.clear();
    }
    
}
