/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lt.lb.commons.threads;

import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 *
 * @author laim0nas100
 */
public class FastExecutor implements Executor {

    protected final ConcurrentLinkedDeque<Runnable> tasks = new ConcurrentLinkedDeque<>();

    protected int maxThreads; // 
    protected AtomicInteger startingThreads = new AtomicInteger(0);
    protected AtomicInteger runningThreads = new AtomicInteger(0);
    protected AtomicInteger finishingThreads = new AtomicInteger(0);
    
    protected Consumer<Throwable> errorChannel = (err) -> {
    };

    /**
     *
     * @param maxThreads 
     * positive limited threads
     * negative unlimited threads
     * zero no threads, execute during update
     * 
     */
    public FastExecutor(int maxThreads) {
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
        update(this.maxThreads);
    }

    protected Runnable getRun(final int bakedMax) {
        return () -> {
            runningThreads.incrementAndGet();
            startingThreads.decrementAndGet(); //thread started

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
                finishingThreads.incrementAndGet(); // thread is finishing
                runningThreads.decrementAndGet(); //thread no longer running (not really)

                update(bakedMax);
                finishingThreads.decrementAndGet(); // thread end finishing
            }
        };
    }

    protected void update(int maxT) {
        if (tasks.isEmpty()) {
            return;
        }
        this.maybeStartThread(maxT);

    }

    protected void startThread(final int maxT) {
        Thread t = new Thread(getRun(maxT));
        t.setName("Fast Executor " + t.getName());
        t.start();
    }

    protected void maybeStartThread(final int maxT) {

        if (maxT == 0) {
            startingThreads.incrementAndGet();//because run decrements
            getRun(maxT).run();
        } else if (maxT < 0) {//unlimited threads
            startingThreads.incrementAndGet();//because run decrements
            startThread(maxT);
        } else { // limitedThreads
            final int starting = startingThreads.incrementAndGet();
            if (starting > maxT) { // we dispatch threads too often
                this.startingThreads.decrementAndGet();
            } else if (starting + runningThreads.get() <= maxT) { // we are within limit
                startThread(maxT);
            } else { // don't start new thread, just update value
                this.startingThreads.decrementAndGet();
            }
        }
    }
}
