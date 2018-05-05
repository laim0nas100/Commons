/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lt.lb.commons.Threads;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;

/**
 *
 * @author Lemmin
 */
public class DynamicBatchExecutor extends DynamicTaskExecutor {

    public DynamicBatchExecutor(int active) {
        this.setRunnerSize(active);
    }
    private ArrayDeque<Callable> tasks = new ArrayDeque<>();

    public void prepareBatch(Callable run) {
        tasks.add(run);
    }

    public void executeBatchAndWait() {
        try {
            this.submitBatchAndWait(tasks);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            tasks.clear();
        }

    }

    public void submitBatchAndWait(Collection<Callable> tasks) throws InterruptedException {
        CountDownLatch l = new CountDownLatch(tasks.size());
        for (Callable call : tasks) {
            Runnable newCall = () -> {
                try {
                    call.call();
                } catch (Exception ex) {
                }
                l.countDown();
            };
            this.submit(newCall);
        }
        l.await();

    }
}
