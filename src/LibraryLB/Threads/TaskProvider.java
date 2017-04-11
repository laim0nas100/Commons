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
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author Lemmin
 */
public class TaskProvider {
    public LinkedBlockingDeque<RunnableFuture> tasks = new LinkedBlockingDeque<>();
    public Runnable onTaskRequest;
    public void submit(RunnableFuture task, boolean priority){
        if(priority){
           this.tasks.addFirst(task);
        }else{
            this.tasks.addLast(task);
        }
        
    }   
    public void submit(Callable call, boolean priority){
        
        submit(new FutureTask(call), priority);
    }
    public void submit(Runnable run, boolean priority){
        submit(new FutureTask(run,null), priority);
    }


    
    public RunnableFuture requestTask() throws InterruptedException{
        
        RunnableFuture t = tasks.pollFirst(1,TimeUnit.MILLISECONDS);
        Log.print("Task request",t!=null);
        if(onTaskRequest!=null){
            onTaskRequest.run();
        }
        return t;
    }
    
}
