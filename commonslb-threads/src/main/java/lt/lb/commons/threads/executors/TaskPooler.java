/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lt.lb.commons.threads.executors;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 * @author laim0nas100
 */
public class TaskPooler extends AbstractExecutorService implements Runnable {

    private int maxCount = 1;
    private AtomicInteger size = new AtomicInteger(0);
    private int threadsFinished = 0;
    private AtomicBoolean wait = new AtomicBoolean(false);
    private boolean shutdownCalled = false;
    private FutureTask<Integer> shutdownCall = new FutureTask<>(() -> 0);
    private ConcurrentLinkedDeque<RunnableFuture> tasks = new ConcurrentLinkedDeque<>();
    private ConcurrentLinkedDeque<RunnableFuture> activeTasks = new ConcurrentLinkedDeque<>();

    private void startThread(RunnableFuture task) {
        if (task == null) {
            return;
        }
        new Thread(task).start();
        activeTasks.add(task);
    }

    private void emptyDoneTasks() {
        Iterator<RunnableFuture> iterator2 = this.activeTasks.iterator();
        while (iterator2.hasNext()) {
            Future next = iterator2.next();
            if (next.isDone()) {
                iterator2.remove();
                this.threadsFinished += 1;
            }
        }
    }

    @Override
    public void run() {
//        Log.print("Executor started");
        while (true) {
            emptyDoneTasks();
            while (!shutdownCalled && !tasks.isEmpty() && activeTasks.size() < maxCount) {
                RunnableFuture first = tasks.pollFirst();
                startThread(first);
                await();
            }
            if (activeTasks.isEmpty() && tasks.isEmpty()) {
                break;
            }
            if (tasks.isEmpty() && activeTasks.isEmpty()) {
                requestWait();
            }
            await();
        }
        shutdownCall.run();
//        Log.print("Executor ended");
        cancelAllTasks();
    }

    public void cancelAllTasks() {
//        Log.print("CANCEL ALL TASKS");
        cancelRunningTasks();
        for (Future task : tasks) {
            task.cancel(true);
        }

    }

    public void cancelRunningTasks() {
        for (Future task : activeTasks) {
            task.cancel(true);
//            Log.print("Cancel active task");
        }
    }

    public void stopEverythingStartThis(Runnable task) {

        this.cancelRunningTasks();
        this.tasks.clear();
        this.activeTasks.clear();
        this.submit(task);

    }

    public void clearSubmittedTasks() {
        tasks.clear();
    }

    private void requestWait() {
        wait.set(true);
    }

    private synchronized void wakeUp() {
        wait.set(false);
        this.notifyAll();
    }

    private synchronized void await() {
        while (wait.get()) {
            try {
                wait();
            } catch (InterruptedException ex) {
            }
        }
    }

    @Override
    public void shutdown() {
        shutdownCalled = true;
    }

    @Override
    public List<Runnable> shutdownNow() {
        shutdown();
        List<Runnable> list = new ArrayList<>();
        RunnableFuture first = tasks.pollFirst();
        while (first != null) {
            list.add(first);
            first = tasks.pollFirst();
        }
        return list;
    }

    @Override
    public boolean isShutdown() {
        return shutdownCalled;
    }

    @Override
    public boolean isTerminated() {
        return activeTasks.isEmpty() && tasks.isEmpty();
    }

    @Override
    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        Object get = null;
        try {
            get = this.shutdownCall.get(timeout, unit);
        } catch (ExecutionException | TimeoutException ex) {
        }
        return get != null;
    }

    @Override
    public void execute(Runnable command) {
        if (this.shutdownCalled) {
            return;
        }
        Runnable wake = () -> {
            command.run();
            wakeUp();

        };
        this.startThread(new FutureTask<>(Executors.callable(wake)));
    }

    public TaskPooler(int maxCount) {
        this.maxCount = Math.max(1, maxCount);
    }
}
