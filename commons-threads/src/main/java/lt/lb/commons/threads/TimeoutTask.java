/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lt.lb.commons.threads;

import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

/**
 *
 * @author Lemmin
 */
public class TimeoutTask {

    private final long timeout;
    private final long refreshRate;
    private final long epsilon = 10; //sleep time window
    private Thread thread;
    private volatile long initTime;
    public BooleanProperty conditionalCheck;
    private ArrayList<Runnable> onUpdate = new ArrayList<>();
    private final Runnable run;
    private final Callable<Void> call = new Callable<Void>() {

        @Override
        public Void call() throws Exception {
            long timeLeft = getTimeLeft();
            do {
                if (timeLeft > timeout) {
                    timeLeft = timeout;
                }
                if (timeLeft > 0) {
//                    Log.print("Sleep TimeLeft = ",timeLeft);
                    Thread.sleep(timeLeft);

                }
                timeLeft = getTimeLeft();
            } while (timeLeft > 0);
            tryRun();
            return null;
        }
    };

    /**
     *
     * @param timeout Milliseconds to wait until execution
     * @param refreshRate Timer update rate (Milliseconds) after timeout was
     * reached
     * @param run Task to execute after timer reaches zero
     */
    public TimeoutTask(long timeout, long refreshRate, Runnable run) {
        this.timeout = timeout;
        this.refreshRate = refreshRate;
        this.initTime = 0;
        conditionalCheck = new SimpleBooleanProperty(true);
        this.run = run;
        this.thread = new Thread();
    }

    /**
     * start timer or update timer if already started
     */
    public void update() {
        for (Runnable r : this.onUpdate) {
            r.run();
        }
        this.initTime = System.currentTimeMillis();
//       Log.print("Init time "+this.initTime);
        if (!thread.isAlive()) {
//           Log.print("Start new thread cus old is dead");
            this.startNewThread();
        }
    }

    private void startNewThread() {
        Thread newThread = new Thread(new FutureTask<>(call));
        newThread.start();
        this.thread = newThread;
    }

    private void tryRun() throws InterruptedException {
        while (!this.conditionalCheck.get()) {
//            Log.print("Failed conditional check");

            Thread.sleep(this.refreshRate);
            if (this.getTimeLeft() > 0) {
//                Log.print("Failed conditional, start new");
                startNewThread();

                return;
            }
        }
        run.run();
//        Log.print("TimoutTask success");
    }

    private long getTimeLeft() {
        return timeout - (System.currentTimeMillis() - this.initTime) + epsilon;
    }

    public boolean isInAction() {
        return this.thread.isAlive();
    }

    public void addOnUpdate(Runnable run) {
        this.onUpdate.add(run);
    }
}
