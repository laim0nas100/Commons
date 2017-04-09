/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package LibraryLB.Threads;

import LibraryLB.Log;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import javafx.beans.property.SimpleBooleanProperty;

/**
 *
 * @author Lemmin
 * @param <T>
 */
public abstract class ExtTask <T> implements Runnable,Future{
    
    public final SimpleBooleanProperty canceled = new SimpleBooleanProperty(false);
    public final SimpleBooleanProperty paused = new SimpleBooleanProperty(false);
    public final SimpleBooleanProperty done = new SimpleBooleanProperty(false);
    public final SimpleBooleanProperty failed = new SimpleBooleanProperty(false);
    public final SimpleBooleanProperty interrupted = new SimpleBooleanProperty(false);
    public ExtTask childTask;
    private LinkedBlockingDeque<T> result = new LinkedBlockingDeque<>();
    private InvokeChildTask onInterrupted,onDone,onFailed,onCanceled,onSucceded;
    public int timesToRun = -1;
    public int timesRan = 0;
    public static interface InvokeChildTask{
        public void handle(Runnable r) throws Exception;
    }
    @Override
    public final void run() {
        if(timesToRun < timesRan && timesToRun >0){
            return;
        }
        boolean ok = true;
        try {
            Log.print("BEGIN");
            T res = call();
            if(res!=null)
                result.offerLast(res);
            Log.print("AFTER CALL");
        } catch (InterruptedException ex) {
            
            tryRun(onInterrupted);
            
        } catch (Exception ex) {
            ok = false;
            tryRun(onFailed);
            failed.set(true);
        }
        if(canceled.get()){
            ok = false;
            tryRun(onCanceled);
        }
        if(ok){
            tryRun(onSucceded);
        }
        
        done.set(true);
        tryRun(onDone);
        timesRan++;
    }
    
    
    private void tryRun(InvokeChildTask r){
        if(r != null){
            try{
                r.handle(this.childTask);
            }catch (Exception e) {}  
        }else{
            Log.print("Nothing to try run");
        }
    }
    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        canceled.set(true);
        return false;
    };
    
    public boolean cancel(){
        return this.cancel(true);
    }

    @Override
    public boolean isCancelled() {
        return this.canceled.get();
    }

    @Override
    public boolean isDone() {
        return done.get();
    }

    @Override
    public Object get() throws InterruptedException, ExecutionException {
        return result.pollFirst();
    }

    @Override
    public Object get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        return result.pollFirst(timeout, unit);
    }

    protected abstract T call() throws Exception;

    public final void setOnFailed(InvokeChildTask handle) {
        this.onFailed = handle;
    }
    public final void setOnSucceeded(InvokeChildTask handle) {
        this.onSucceded = handle;
    }
    public final void setOnCancelled(InvokeChildTask handle) {
        this.onCanceled = handle;
    }
    public final void setOnInterrupted(InvokeChildTask handle){
        this.onInterrupted = handle;
    }

    
    public Thread toThread(){
        return new Thread(this);
    }
    
}
