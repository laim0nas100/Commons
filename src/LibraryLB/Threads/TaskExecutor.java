/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package LibraryLB.Threads;

import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedDeque;
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
    public boolean neverStop = false;
    private ConcurrentLinkedDeque<Task> tasks = new ConcurrentLinkedDeque<>();
    private ConcurrentLinkedDeque<Thread> threads = new ConcurrentLinkedDeque<>();

    public TaskExecutor(int maxCount, int refreshDuration){
        if(maxCount>0){
            this.maxCount = maxCount;
        }
        if(refreshDuration>1){
            this.refreshDuration = refreshDuration;
        }
        this.size = new AtomicInteger(0);
        
    }
    private void startThread(Runnable run){
        Thread t = new Thread(run);
        threads.addFirst(t);
        t.start();
    }
    private void emptyDeadThreads(){
        Iterator<Thread> iterator = this.threads.iterator();
        while(iterator.hasNext()){
            Thread next = iterator.next();
            if(!next.isAlive()){
                iterator.remove();
                this.threadsFinished+=1;
            }
        }
    }

    public void addTask(Task task){
        this.size.getAndIncrement();
        this.tasks.addLast(task);
    }

    @Override
    protected Void call() throws Exception {
        while(!this.isCancelled()){
            emptyDeadThreads();
            while(!tasks.isEmpty() && threads.size()<maxCount){
                Task pollFirst = tasks.pollFirst();
                startThread(pollFirst);
            }
            
            this.updateProgress(threadsFinished, size.get());
            try {
                Thread.sleep(refreshDuration);
            } catch (InterruptedException ex) {
                break;
            }
            if(threads.isEmpty()&&tasks.isEmpty()&&!neverStop){
               return null; 
            }
        }
        tasks.forEach(task->{
            task.cancel();
        });
        return null;
    }

    
}
