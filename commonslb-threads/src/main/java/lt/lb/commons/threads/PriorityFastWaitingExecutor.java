/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lt.lb.commons.threads;

import java.util.concurrent.PriorityBlockingQueue;
import lt.lb.commons.F;
import lt.lb.commons.Lambda;
import lt.lb.commons.misc.ExtComparator;
import lt.lb.commons.threads.sync.WaitTime;

/**
 *
 * @author laim0nas100
 */
public class PriorityFastWaitingExecutor extends FastWaitingExecutor {

    private Lambda.L1R<Runnable, Integer> mapping = (r) -> {
        if (r instanceof PriorityRunnable) {
            PriorityRunnable pr = F.cast(r);
            return pr.order;
        }
        return 0;
    };
    private ExtComparator<Runnable> priorityComparator = ExtComparator.ofValue(mapping, ExtComparator.ofComparable(false));

    private class PriorityRunnable implements Runnable {

        int order;
        Runnable ru;

        public PriorityRunnable(Runnable r, int order) {
            this.ru = r;
            this.order = order;
        }

        public void run() {
            this.ru.run();
        }
    }

    /**
     *
     * @inheritDoc
     */
    public PriorityFastWaitingExecutor(int maxThreads) {
        this(maxThreads, WaitTime.ofSeconds(1));
    }

    /**
     * @inheritDoc
     */
    public PriorityFastWaitingExecutor(int maxThreads, WaitTime time) {
        super(maxThreads);
        this.tasks = new PriorityBlockingQueue<>(1, priorityComparator.reversed());
        this.wt = time;
    }

    @Override
    protected Runnable getMainBody() {
        return () -> {
            PriorityBlockingQueue<Runnable> deque = F.cast(tasks);
            Runnable last = null;
            do {

                try {

                    last = deque.poll(wt.time, wt.unit);
                } catch (InterruptedException ex) {
                }
                if (last != null) {
                    last.run();
                }

            } while (!deque.isEmpty() || last != null);
        };
    }

    @Override
    protected void startThread(final int maxT) {
        Thread t = new Thread(getRun(maxT));
        t.setName("Priority Fast Waiting Executor " + t.getName());
        t.start();
    }

    /**
     * Higher priority means execute sooner. If priority below 0, executes after
     * runnables without priority
     *
     * @param priority
     * @param run
     */
    public void execute(int priority, Runnable run) {
        this.execute(new PriorityRunnable(run, priority));
    }

    @Override
    public void execute(Runnable command) {
        PriorityBlockingQueue<Runnable> cast = F.cast(tasks);
        cast.add(command);
        update(this.maxThreads);
    }

}
