/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package LibraryLB.Threads;

import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicLong;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

/**
 *
 * @author Lemmin
 */
public class TimeoutTask{
    private final long timeout;
    private final long refreshRate;
    private AtomicLong currentTime;
    private boolean initiated;
    public BooleanProperty conditionalCheck;
    private ArrayList<Runnable> onUpdate = new ArrayList<>();
    private final Runnable run;
    private final Callable call = new Callable() {

        @Override
        public Void call() throws Exception {
//            Log.write("Sleep:"+currentTime.get());
            Thread.sleep(timeout);
            currentTime.decrementAndGet();
//            Log.write("Try run:"+currentTime.get());
            tryRun();
            return null;
        }
    };
    /**
     *
     * @param timeout Milliseconds to wait until execution
     * @param refreshRate  Timer update rate (Milliseconds)
     * @param run   Task to execute after timer reaches zero
     */
    public TimeoutTask(long timeout,long refreshRate,Runnable run){
        this.timeout = timeout;
        this.refreshRate = refreshRate;
        this.currentTime = new AtomicLong(0);
        initiated = false;
        conditionalCheck = new SimpleBooleanProperty(true);
        this.run = run;
    }
    
    /**
     *  start timer or update timer if already started
     */
    public void update(){
       for(Runnable r:this.onUpdate){
           r.run();
       }
       currentTime.incrementAndGet();
       initiated = true;
       
       new Thread(new FutureTask(call)).start();
       
       
       
    }
    private void tryRun() throws InterruptedException{
        if(initiated){
            if(this.currentTime.get()<=0){
                while(!this.conditionalCheck.get()){
//                    Log.write("Failed conditional check");
                    Thread.sleep(this.refreshRate);
                    if(!initiated || this.currentTime.get()>0){
                        return;
                    }
                }
                initiated = false;
                run.run();
//                Log.write("TimoutTask success");
            }  
        }
    }
    
    public boolean isInAction(){
        return this.initiated;
    }
    public void addOnUpdate(Runnable run){
        this.onUpdate.add(run);
    }
}