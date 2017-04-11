/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package LibraryLB.Threads;

import LibraryLB.Log;
import java.util.concurrent.RunnableFuture;

/**
 *
 * @author Lemmin
 */
public abstract class TaskRunner implements Runnable{
    public RunnableFuture task;
    public Runnable onRunFinished;
    public TaskProvider provider;
    public volatile boolean active = true;
    public Thread me;
    
    public TaskRunner(TaskProvider provider,Runnable onRunFinished){
        this.provider = provider;
        this.onRunFinished = onRunFinished;
    }
    public void disable(){
        this.active = false;
    }
    public abstract void wakeUp();
    @Override
    public final void run() {
        Log.print("Runner started");
        while(active){
            try{
                 commenceRun();           
            }catch (InterruptedException ex){
                Log.print("INTERRUPTED");
            }
        }
        Log.print("Runner ended");
    }
    
    protected abstract void commenceRun() throws InterruptedException;

    
    
}
