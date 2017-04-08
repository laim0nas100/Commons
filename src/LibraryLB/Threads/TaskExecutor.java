/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package LibraryLB.Threads;

import LibraryLB.Log;
import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicInteger;
import javafx.concurrent.Task;

/**
 *
 * @author Lemmin
 */
public class TaskExecutor extends ExtTask{
    private int maxCount = 1;
    private AtomicInteger size;
    private int threadsFinished = 0;
    private CountDownLatch latch;
    public boolean neverStop = false;
    private ConcurrentLinkedDeque<Task> tasks = new ConcurrentLinkedDeque<>();
    private ConcurrentLinkedDeque<Task> activeTasks = new ConcurrentLinkedDeque<>();
    public TaskExecutor(int maxCount, int refreshDuration){
        if(maxCount>0){
            this.maxCount = maxCount;
        }
        if(refreshDuration>=0){
            this.refreshDuration = refreshDuration;
        }
        this.size = new AtomicInteger(0);
    }
    public TaskExecutor(int maxCount, int refreshDuration, boolean neverStop){
        this(maxCount, refreshDuration);
        this.neverStop = neverStop;
    }
    private void startThread(Task task){
        if(task == null){
            return;
        }
        new Thread(task).start();
        activeTasks.add(task);
    }
    private void emptyDoneTasks(){
        Iterator<Task> iterator2 = this.activeTasks.iterator();
        while(iterator2.hasNext()){
            Task next = iterator2.next();
            if(next.isDone()){
                iterator2.remove();
                this.threadsFinished+=1;
            }
        }
    }

    public void submit(Task task){
        this.size.getAndIncrement();
        this.tasks.addLast(task);
    }
    
    public void submit(Callable call){
        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                call.call();
                return null;
            }
        };
        submit(task);
    }
    public void submit(Runnable run){
        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                run.run();
                return null;
            }
        };
        submit(task);
    }

    @Override
    protected Void call() throws Exception {
//        Log.print("Executor started");
        prepareLatch();
        latch.countDown();
        while(!this.isCancelled()){
            latch.await();
            emptyDoneTasks();
            while(!tasks.isEmpty() && activeTasks.size()<maxCount){
                Task first = tasks.pollFirst();
                startThread(first);
                latch.await();
            }
            
            this.updateProgress(threadsFinished, size.get());
            try {
                if(refreshDuration>0){
                   Thread.sleep(refreshDuration); 
                }  
            } catch (InterruptedException ex) {
                break;
            }
            if(activeTasks.isEmpty()&&tasks.isEmpty()&&!neverStop){
               return null; 
            }
        }
//        Log.print("Executor ended");
        cancelAllTasks();
        return null;
    }

    public void cancelAllTasks(){
        Log.print("CANCEL ALL TASKS");
        cancelRunningTasks();
        for(Task task:tasks){
            task.cancel();
        }
        
        
    }
    public void cancelRunningTasks(){
        for(Task task:activeTasks){
            task.cancel();
            Log.print("Cancel active task");
        }
    }
    
    public void stopEverythingStartThis(Task task){
        prepareLatch();
        this.cancelRunningTasks();
        this.tasks.clear();
        this.activeTasks.clear();
        this.submit(task);
        latch.countDown();
    }
    
    public void clearSubmittedTasks(){
        tasks.clear();
    }
    
    private void prepareLatch(){
        this.latch = new CountDownLatch(1);
    }
}
