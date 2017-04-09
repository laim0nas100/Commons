/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package LibraryLB.Threads;

import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 *
 * @author Lemmin
 */
public class RepeatableTask implements Runnable{
    private AtomicBoolean ready = new AtomicBoolean(true);
    private Callable call;
    public RepeatableTask(Runnable task){
        this.call = Executors.callable(task, null);
    }
    public RepeatableTask(Callable task){
        this.call = task;
    }

    @Override
    public void run() {
        if(ready.compareAndSet(true, false)){
            try {
                call.call();
                ready.set(true);
            } catch (Exception ex) {
            }
        }
    }
    
}
