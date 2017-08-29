/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package LibraryLB.Threads;

import java.util.concurrent.RunnableFuture;

/**
 *
 * @author Lemmin
 */
public abstract class TaskRunner extends ExtTask implements Comparable{
    public RunnableFuture task;
    public Runnable onRunFinished;
    public TaskProvider provider;
    public volatile boolean active = true;
    public Thread me;
    public final Long creationTime;
    
    public TaskRunner(TaskProvider provider,Runnable onRunFinished){
        this.provider = provider;
        this.onRunFinished = onRunFinished;
        this.creationTime = System.currentTimeMillis();
    }
    public void disable(){
        this.active = false;
    }
    public abstract void wakeUp();
    @Override
    public final Integer call() {
//        Log.print("Runner started");
        while(active){
            try{
                commenceRun();           
            }catch (InterruptedException ex){
//                Log.print("INTERRUPTED");
            }
        }
//        Log.print("Runner ended");
        return 0;
    }
    
    protected abstract void commenceRun() throws InterruptedException;
    @Override
    public int compareTo(Object o) {
        if(o instanceof TaskRunner){
            TaskRunner runner = (TaskRunner) o;
            return this.creationTime.compareTo(runner.creationTime);
        }
        return -1; 
    }
    
    
}
