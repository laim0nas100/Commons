/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package LibraryLB.Threads;

import LibraryLB.Log;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.Callable;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Exchanger;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.concurrent.Task;

/**
 *
 * @author Lemmin
 */
public class TaskRunner implements Callable{
    public RunnableFuture task;
    public Runnable onRunFinished;
    public DynamicTaskExecutor executor;
    public boolean active = true;
    public AtomicBoolean sleeping = new AtomicBoolean(false);
    private CyclicBarrier gate = new CyclicBarrier(2);
    
    public TaskRunner(DynamicTaskExecutor executor,Runnable onRunFinished){
        this.executor = executor;
        this.onRunFinished = onRunFinished;
    }
    @Override
    public Object call() throws Exception{
        while(active){
            RunnableFuture t = executor.provider.requestTask();
            if(t == null){
                requestSleep();
            }else{
                task = t;
                task.run();
                onRunFinished.run();
            }
            await();
        }
        return 0;
    }
    
    public void requestSleep(){
        sleeping.set(true);
    }
    public void await(){
        try {
            executor.sleepingCount.incrementAndGet();
            Log.print("Sleeping",Thread.currentThread().getName());
            while(sleeping.get() && active){
                gate.await(1, TimeUnit.MINUTES);
            }
            Log.print("Wakeup",Thread.currentThread().getName());
            executor.sleepingCount.decrementAndGet();
        } catch (InterruptedException | TimeoutException ex) {} catch (BrokenBarrierException ex) {
        }
    }
    public void wakeUp(){
        if(sleeping.compareAndSet(true,false)){
            gate.reset();
        }
    }
    public void disable(){
        this.active = false;
        wakeUp();
    }
//    public void cancel(){
//        if(task!=null){
//            Log.print("Active task canceled");
//            if(task instanceof Task){
//                Task t = (Task)task;
//                t.cancel(true);
//            }else{
//                task.cancel(true);
//            }
//            
//            
//        }
//    }

    
    
}
