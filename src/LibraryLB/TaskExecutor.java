/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package LibraryLB;

import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedDeque;
import javafx.beans.property.SimpleLongProperty;
import javafx.concurrent.Task;

/**
 *
 * @author Lemmin
 */
public class TaskExecutor extends ExtTask{
    private int maxCount = 1;
    private int size = 0;
    private int threadsFinished = 0;
    private ConcurrentLinkedDeque<Task> tasks = new ConcurrentLinkedDeque<>();
    private ConcurrentLinkedDeque<Thread> threads = new ConcurrentLinkedDeque<>();

    public TaskExecutor(int maxCount, int refreshDuration){
        if(maxCount>0){
            this.maxCount = maxCount;
        }
        if(refreshDuration>1){
            this.refreshDuration = refreshDuration;
        }
        
    }
    private void startThread(Runnable run){
        Thread t = new Thread(run);
        this.threads.addFirst(t);
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
        this.size+=1;
        this.tasks.addFirst(task);
    }

    @Override
    protected Void call() throws Exception {
        while(!this.isCancelled()){
            while(!tasks.isEmpty() && threads.size()<maxCount){
                Task pollFirst = tasks.pollFirst();
                startThread(pollFirst);
            }
            emptyDeadThreads();
            this.updateProgress(threadsFinished, size);
            try {
                Thread.sleep(refreshDuration);
            } catch (InterruptedException ex) {
                break;
            }
        }
        tasks.forEach(task->{
            task.cancel();
        });
        return null;
    }

    
}
