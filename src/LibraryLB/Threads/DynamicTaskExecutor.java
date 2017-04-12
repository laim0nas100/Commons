/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package LibraryLB.Threads;

import LibraryLB.Log;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author Lemmin
 */
public class DynamicTaskExecutor extends AbstractExecutorService{
    protected TaskProvider provider = new TaskProvider();
    protected ConcurrentLinkedDeque<TaskRunner> activeRunners = new ConcurrentLinkedDeque<>();
    protected ConcurrentSkipListSet<TaskRunner> disabledRunners = new ConcurrentSkipListSet<>();
    protected BigInteger tasksFinished = BigInteger.ZERO;
    protected BigInteger tasksAdded = BigInteger.ZERO;
    protected Runnable doOnFinish = () -> increment();
    protected int activeCount = 0;
    protected boolean shutdownCalled = false;
    protected ExtTask terminationTask = new ExtTask(-1) {
        @Override
        protected Object call() throws Exception {
            List<TaskRunner> list = new ArrayList<>();
            list.addAll(disabledRunners);
            list.addAll(activeRunners);
            Log.print("Termination task running");
            for(TaskRunner runner:list){
                runner.wakeUp();
                runner.me.join();
                Log.print("Joined!!!");
            }
            Log.print("Termination task finished");
            return 0;
        }
    };
    public void wakeUpRunners(){      
        for(TaskRunner runner:activeRunners){
            runner.wakeUp();
        }
    }
    protected synchronized void increment(){
        tasksFinished.add(BigInteger.ONE);
    }
    protected TaskRunner createRunner(Runnable doOnFinish){      
        TaskRunner[] runner = new TaskRunner[1];
        TaskRunner tryMe = disabledRunners.pollLast();
        if(tryMe == null || tryMe.me.isAlive()){
            if(tryMe != null && tryMe.me.isAlive()){
                disabledRunners.add(tryMe);
            }
        }else{
            tryMe.active = true;
            runner[0] = tryMe;
            Log.print("Reuse taskrunner");
        }
        if(runner[0] == null){
            runner[0] = new DynamicTaskRunner(provider,doOnFinish); 
            Log.print("Create new taskrunner");
        }
        runner[0].setOnDone(ondone ->{
            Log.print("Removed runner from set");
            disabledRunners.remove(runner[0]);
        });
        activeRunners.add(runner[0]);
        Thread t = new Thread(runner[0]);
        runner[0].me = t;
        runner[0].reset();
        return runner[0];
    }
    public void setRunnerSize(int size){
        activeCount = Math.max(size, 0);
        while(activeRunners.size() <  activeCount){
            createRunner(this.doOnFinish).me.start();
        }
        while(activeRunners.size() > activeCount){
            TaskRunner runner = activeRunners.pollFirst();
            runner.disable();
            this.disabledRunners.add(runner);
        }
        Log.print("New runner size",this.activeCount);
    }
    
    public void cancelAllTasks(){
        
        for(TaskRunner runner:this.activeRunners){
            if(runner.task != null){
                runner.task.cancel(true);
            }
        }
        for(RunnableFuture task:provider.tasks){
            task.cancel(true);
        }
    }
    public void stopEverything(){
        clearPendingTasks();
        cancelAllTasks();

    }
    public void stopEverythingStartThis(Runnable runnable){
        stopEverything();
        provider.submit(runnable, true);
        wakeUpRunners();
    }
    public void clearPendingTasks(){
        provider.tasks.clear();
    }

    @Override
    public void shutdown() {      
        Log.print("Shutdown call");
        if(!shutdownCalled){
            Log.print("Shutdown commance");
            Runnable checkEmpty = () ->{
                if(provider.tasks.isEmpty()){
                    setRunnerSize(0);
                }
                Log.print("Update size",provider.tasks.size());
            };
            checkEmpty.run();
            provider.onTaskRequest = checkEmpty;
            shutdownCalled = true;
            Log.print("start task");
            terminationTask.toThread().start();
        }
    }

    @Override
    public List<Runnable> shutdownNow() {
        shutdown();
        List<Runnable> list = Collections.emptyList();
        Runnable run = provider.tasks.pollFirst();
        while(run != null){
            list.add(run);
            run = provider.tasks.pollFirst();
        }
        return list;
    }

    @Override
    public boolean isShutdown() {
        return this.activeCount == 0;
    }

    @Override
    public boolean isTerminated() {
        boolean allDead = isShutdown();
        Iterator<TaskRunner> iterator = this.disabledRunners.iterator();
        while(allDead){
            if(iterator.hasNext()){
                allDead = !(iterator.next().me.isAlive());
            }
        }
        return allDead;
    }

    @Override
    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        Object get = terminationTask.get(timeout, unit);
        return get != null;
    
    }

    @Override
    public synchronized void execute(Runnable command) {
        if(!shutdownCalled){
            provider.submit(command,false);
            this.tasksAdded.add(BigInteger.ONE);
        }
        wakeUpRunners();
    }
    
}
