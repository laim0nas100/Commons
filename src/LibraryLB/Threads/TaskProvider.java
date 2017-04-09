/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package LibraryLB.Threads;

import LibraryLB.Log;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Lemmin
 */
public class TaskProvider {
    public AtomicInteger submitted = new AtomicInteger(0);
    public LinkedBlockingDeque<RunnableFuture> tasks = new LinkedBlockingDeque<>();
    public LinkedBlockingDeque<RunnableFuture> activeTasks = new LinkedBlockingDeque<>();
    public void submit(RunnableFuture task, boolean priority){
        this.submitted.getAndIncrement();
        if(priority){
           this.tasks.addFirst(task);
        }else{
            this.tasks.addFirst(task);
        }
        
    }   
    public void submit(Callable call, boolean priority){
        
        submit(new FutureTask(call), priority);
    }
    public void submit(Runnable run, boolean priority){
        submit(new FutureTask(run,null), priority);
    }


    
    public RunnableFuture requestTask() throws InterruptedException{
        
        RunnableFuture t = tasks.poll(1,TimeUnit.MILLISECONDS);
        Log.print("Task request",t!=null);
        if(t!=null){
            activeTasks.add(t);
        }
        return t;
    }
    
}
