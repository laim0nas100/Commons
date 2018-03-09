/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package LibraryLB.Threads;

import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.Executor;

/**
 *
 * @author Laimonas-Beniusis-PC
 */
public class DisposableExecutor implements Executor {

    final ConcurrentLinkedDeque<Runnable> tasks = new ConcurrentLinkedDeque<>();
    final ConcurrentLinkedDeque<Thread> threads = new ConcurrentLinkedDeque<>();

    protected int maxThreads;

    public DisposableExecutor(int maxThreads) {
        this.maxThreads = maxThreads;
    }

    @Override
    public void execute(Runnable command) {
        tasks.addFirst(command);
        update();
    }

    protected void update() {
        //scan dead
        synchronized (threads) {
            Iterator<Thread> iterator = threads.iterator();
            while (iterator.hasNext()) {
                Thread next = iterator.next();
                if (next.getState().equals(Thread.State.TERMINATED)) {
                    iterator.remove();
                }
            }
        }
        if (threads.size() < maxThreads) {
            Runnable run = () -> {
                try {
                    while (!tasks.isEmpty()) {
                        Runnable last = tasks.pollLast();
                        last.run();
                    }
                } finally {
                    if (!tasks.isEmpty()) {
                        new Thread(
                                () -> {
                                    update();
                                }
                        ).start();
                    }
                }
            };
            Thread thread = new Thread(run);
            thread.start();
            threads.add(thread);

        }

    }
}
