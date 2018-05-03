/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package LibraryLB.Threads;

import java.util.concurrent.RunnableFuture;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author Lemmin
 */
public class ConstantTaskRunner extends TaskRunner {

    public ConstantTaskRunner(TaskProvider provider, Runnable onRunFinished) {
        super(provider, onRunFinished);
    }

    @Override
    public void wakeUp() {
    }

    @Override
    protected void commenceRun() throws InterruptedException {
        RunnableFuture t = provider.tasks.pollFirst(1, TimeUnit.MINUTES);
        if (t != null) {
            task = t;
            task.run();
            onRunFinished.run();
        }
    }

}
