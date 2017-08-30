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
    private final long epsilon = 10; //sleep time window
    private Thread thread;
    private final AtomicLong initTime;
    public BooleanProperty conditionalCheck;
    private ArrayList<Runnable> onUpdate = new ArrayList<>();
    private final Runnable run;
    private final Callable call = new Callable() {

        @Override
        public Void call() throws Exception {
            long timeLeft = 0;
            do{
                timeLeft = getTimeLeft();
                Thread.sleep(timeLeft);
            }while(timeLeft>0);
            tryRun();
            return null;
        }
    };
    /**
     *
     * @param timeout Milliseconds to wait until execution
     * @param refreshRate  Timer update rate (Milliseconds) after timeout was reached
     * @param run   Task to execute after timer reaches zero
     */
    public TimeoutTask(long timeout, long refreshRate, Runnable run){
        this.timeout = timeout;
        this.refreshRate = refreshRate;
        this.initTime = new AtomicLong(0);
        conditionalCheck = new SimpleBooleanProperty(true);
        this.run = run;
        this.thread = new Thread(new FutureTask(call));
    }
    
    /**
     *  start timer or update timer if already started
     */
    public void update(){
       for(Runnable r:this.onUpdate){
           r.run();
       }
       this.initTime.set(System.currentTimeMillis());
       if(!thread.isAlive()){
           this.startNewThread();
       }
    }
    
    
    private void startNewThread(){
        thread = new Thread(new FutureTask(call));
        thread.start();
    }
    
    private void tryRun() throws InterruptedException{
        while(!this.conditionalCheck.get()){
//          Log.write("Failed conditional check");
            Thread.sleep(this.refreshRate);
            if(this.getTimeLeft()>0){
                startNewThread();
                return;
            }
        }
        run.run();
//      Log.write("TimoutTask success"); 
    }
    
    private long getTimeLeft(){
        return timeout - (System.currentTimeMillis() - this.initTime.get()) + epsilon;
    }
    
    public boolean isInAction(){
        return this.thread.isAlive();
    }
    
    public void addOnUpdate(Runnable run){
        this.onUpdate.add(run);
    }
}