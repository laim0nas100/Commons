/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lt.lb.commons.threads;

import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 *
 * @author laim0nas100
 * use FastExecutor
 */
@Deprecated
public class DisposableExecutor implements Executor {

    protected final ConcurrentLinkedDeque<Runnable> tasks = new ConcurrentLinkedDeque<>();
    protected final ConcurrentLinkedDeque<Thread> threads = new ConcurrentLinkedDeque<>();

    protected int maxThreads;
    protected AtomicInteger startingThreads = new AtomicInteger(0);
    protected Consumer<Throwable> errorChannel = (err) -> {
    };

    public DisposableExecutor(int maxThreads) {
        this.maxThreads = maxThreads;
    }

    public void setMaxThreads(int maxThreads) {
        this.maxThreads = maxThreads;
    }

    public void setErrorChannel(Consumer<Throwable> channel) {
        errorChannel = channel;
    }

    public Consumer<Throwable> getErrorChannel() {
        return errorChannel;
    }

    @Override
    public void execute(Runnable command) {
        tasks.addFirst(command);
        update();
    }

    protected void scanDead() {
        Iterator<Thread> iterator = threads.iterator();
        while (iterator.hasNext()) {
            Thread next = iterator.next();
            if (next.getState().equals(Thread.State.TERMINATED)) {
                iterator.remove();
            }
        }
    }
    protected Runnable run = () -> {
        try {
            while (!tasks.isEmpty()) {
                Runnable last = tasks.pollLast();
                if (last != null) {
                    last.run();
                }

            }
        } catch (Throwable th) {
            try {
                getErrorChannel().accept(th);
            } catch (Throwable err) {// we really screwed now
                err.printStackTrace();
            }
        } finally {
            Thread thread = new Thread(
                    () -> {
                        update();
                    }
            );
            thread.setName(Thread.currentThread().getName() + " Update");
            thread.start();
        }
    };

    protected void update() {

        scanDead();
        if (tasks.isEmpty()) {
            return;
        }
        this.maybeStartThread();

    }

    protected void maybeStartThread() {
        if (this.startingThreads.incrementAndGet() > maxThreads) {
            this.startingThreads.decrementAndGet();
            return;
        }
        if (threads.size() < maxThreads || maxThreads <= 0) {
            Thread thread = new Thread(run);
            threads.add(thread);
            thread.start();
        }
        this.startingThreads.decrementAndGet();
    }
}
