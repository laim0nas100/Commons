/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lt.lb.commons.javafx;

//import lt.lb.commons.Log;
import java.util.Iterator;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 * @author laim0nas100
 */
public class FXTaskPooler extends FXTask {

    private int maxCount = 1;
    private AtomicInteger size = new AtomicInteger(0);
    private int threadsFinished = 0;
    private AtomicBoolean wait = new AtomicBoolean(false);
    public boolean neverStop = false;
    private ConcurrentLinkedDeque<RunnableFuture> tasks = new ConcurrentLinkedDeque<>();
    private ConcurrentLinkedDeque<RunnableFuture> activeTasks = new ConcurrentLinkedDeque<>();

    public FXTaskPooler(int maxCount, int refreshDuration) {
        this.maxCount = Math.max(1, maxCount);
        this.refreshDuration = Math.max(0, refreshDuration);

    }

    public FXTaskPooler(int maxCount, int refreshDuration, boolean neverStop) {
        this(maxCount, refreshDuration);
        this.neverStop = neverStop;
    }

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

    public void submit(Callable call) {
        submit(new FutureTask(call));
    }

    public void submit(Runnable run) {
        if (run instanceof RunnableFuture) {
            RunnableFuture task = (RunnableFuture) run;
            this.size.getAndIncrement();
            this.tasks.addLast(task);
            endWait();
        } else {
            submit(new FutureTask(run, null));
        }
    }

    @Override
    protected Void call() throws Exception {
        while (!this.isCancelled()) {

            if (tasks.isEmpty() && activeTasks.isEmpty()) {
                requestWait();
            }
            await();
            emptyDoneTasks();
            while (!tasks.isEmpty() && activeTasks.size() < maxCount) {
                RunnableFuture first = tasks.pollFirst();
                startThread(first);
                await();
            }
            this.updateProgress(threadsFinished, size.get());
            try {
                if (refreshDuration > 0) {
                    Thread.sleep(refreshDuration);
                }
            } catch (InterruptedException ex) {
                break;
            }
            if (activeTasks.isEmpty() && tasks.isEmpty() && !neverStop) {
                return null;
            }
        }
        cancelAllTasks();
        return null;
    }

    public void cancelAllTasks() {
        cancelRunningTasks();
        for (Future task : tasks) {
            task.cancel(true);
        }

    }

    public void cancelRunningTasks() {
        for (Future task : activeTasks) {
            task.cancel(true);
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

    private synchronized void endWait() {
        wait.set(false);
        this.notify();
    }

    private synchronized void await() throws InterruptedException {
        while (wait.get()) {
            wait();
        }
    }
}
