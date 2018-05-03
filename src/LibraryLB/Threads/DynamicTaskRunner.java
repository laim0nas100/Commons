/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package LibraryLB.Threads;

import java.util.concurrent.RunnableFuture;

/**
 *
 * @author Lemmin
 */
public class DynamicTaskRunner extends TaskRunner {

    public DynamicTaskRunner(TaskProvider provider, Runnable onRunFinished) {
        super(provider, onRunFinished);
    }

    private synchronized void sleep() throws InterruptedException {
//        Log.println("Sleeping");
        this.wait(60000);
//        Log.print("Wake up");
    }

    @Override
    public synchronized void wakeUp() {
        this.notifyAll();
    }

    @Override
    public void disable() {
        this.active = false;
        wakeUp();
    }

    @Override
    protected void commenceRun() throws InterruptedException {
        RunnableFuture t = provider.requestTask();
        if (t == null) {
            sleep();
        } else {
            task = t;
            task.run();
            onRunFinished.run();
        }
    }

}
