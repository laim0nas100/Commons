/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lt.lb.commons.javafx;

import lt.lb.commons.javafx.TaskRunner;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.TimeUnit;
import lt.lb.commons.threads.TaskProvider;

/**
 *
 * @author laim0nas100
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
