/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package LibraryLB.Threads;

import java.util.HashMap;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;

/**
 *
 * @author Lemmin
 * @param <T>
 */
public abstract class ExtTask <T> implements RunnableFuture{
    
    public final ReadOnlyBooleanProperty canceled = new SimpleBooleanProperty(false);
    public final ReadOnlyBooleanProperty paused = new SimpleBooleanProperty(false);
    public final ReadOnlyBooleanProperty done = new SimpleBooleanProperty(false);
    public final ReadOnlyBooleanProperty failed = new SimpleBooleanProperty(false);
    public final ReadOnlyBooleanProperty interrupted = new SimpleBooleanProperty(false);
    public final ReadOnlyBooleanProperty running = new SimpleBooleanProperty(false);

    
    protected HashMap<String, Object> valueMap = new HashMap<>();
    public ExtTask childTask;
    private LinkedBlockingDeque<T> resultDeque = new LinkedBlockingDeque<>();
    private T result;
    private InvokeChildTask onInterrupted,onDone,onFailed,onCanceled,onSucceded;
    private int timesToRun = 1;
    private int timesRan = 0;
    public static interface InvokeChildTask{
        public void handle(Runnable r) throws Exception;
    }
    
    public ExtTask(int timesToRun){
        this.timesToRun = timesToRun;
    }
    public ExtTask(){
        this(1);
    }
    private void setProperty(ReadOnlyBooleanProperty prop, boolean value){
        SimpleBooleanProperty p = (SimpleBooleanProperty) prop;
        p.set(value);
        
    }
    public void reset(){
        setProperty(done,false);
        setProperty(canceled,false);
        setProperty(interrupted,false);
        setProperty(failed,false);
        setProperty(paused,false);
    }
    @Override
    public final void run() {
        if(running.get()||(timesRan >= timesToRun && timesToRun > 0)){
            return;
        }
        
        setProperty(running,true);
        try {
            T res = call();
            if(res!=null){
                resultDeque.offerFirst(res);
            }
            tryRun(onSucceded);
        } catch (InterruptedException ex) {
            setProperty(interrupted,true);
            tryRun(onInterrupted);          
        } catch (Exception ex) {
            setProperty(failed,true);
            tryRun(onFailed);
        }
        if(canceled.get()){
            tryRun(onCanceled);
        }
        setProperty(done,true);
        tryRun(onDone);
        timesRan++;
        setProperty(running,false);
    }
    
    
    private void tryRun(InvokeChildTask r){
        if(r != null){
            try{
                r.handle(this.childTask);
            }catch (Exception e) {}  
        }else{
//            Log.print("Nothing to try run");
        }
    }
    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        setProperty(canceled,true);
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
    public T get() throws InterruptedException {
        if(done.get()){
            return result;
        }else{
            result = resultDeque.pollFirst();
            return result;
        }
        
    }

    @Override
    public T get(long timeout, TimeUnit unit) throws InterruptedException {
        if(done.get()){
            return result;
        }else{
            result = resultDeque.pollFirst(timeout, unit);
            return result;
        }
        
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
    public final void setOnDone(InvokeChildTask handle){
        this.onDone = handle;
    }

    
    public Thread toThread(){
        return new Thread(this);
    }
    
    public static ExtTask<Void> create(Runnable run){
        return new ExtTask<Void>() {
            @Override
            protected Void call() throws Exception {
                run.run();
                return null;
            }
        };
    }
    public static ExtTask create(Callable call){
        return new ExtTask() {
            @Override
            protected Object call() throws Exception {
                return call.call();
            }
        };
    }
    
    public void addObject(String key, Object object){
        this.valueMap.put(key, object);
    }
            
    
}
