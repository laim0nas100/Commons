/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package LibraryLB.Threads;

import LibraryLB.Log;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
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
    public Thread me;
    public AtomicBoolean sleeping = new AtomicBoolean(false);
    private CountDownLatch gate = new CountDownLatch(1);
    
    public TaskRunner(DynamicTaskExecutor executor,Runnable onRunFinished){
        this.executor = executor;
        this.onRunFinished = onRunFinished;
    }
    @Override
    public Object call() throws Exception{
        Log.print("Runner started");
        while(active){
            try{
                RunnableFuture t = executor.provider.tasks.pollFirst(1, TimeUnit.MINUTES);
                if(t==null){
                    continue;
                }
                task = t;
                task.run();
                onRunFinished.run();
            }catch (InterruptedException ex){
                Log.print("INTERRUPTED");
            }

        }
        Log.print("Runner ended");
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
            gate = new CountDownLatch(1);
            Log.print("Wakeup",Thread.currentThread().getName());
            executor.sleepingCount.decrementAndGet();
        } catch (InterruptedException ex) {}
    }
    public void wakeUp(){
//            gate.countDown();
//            me.interrupt();
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
