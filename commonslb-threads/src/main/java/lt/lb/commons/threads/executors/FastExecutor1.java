/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lt.lb.commons.threads.executors;

import java.util.Collection;
import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import lt.lb.commons.F;
import lt.lb.commons.threads.sync.ThreadBoundedState;

/**
 *
 * @author laim0nas100
 */
public class FastExecutor1 implements Executor {

    protected Collection<? extends Runnable> tasks = new ConcurrentLinkedDeque<>();

    protected ThreadBoundedState state;
    protected int maxThreads;

    protected Consumer<Throwable> errorChannel = (err) -> {
    };

    /**
     *
     * @param maxThreads positive limited threads negative unlimited threads
     * zero no threads, execute during update
     *
     */
    public FastExecutor1(int maxThreads) {
        this.maxThreads = maxThreads;
        state = new ThreadBoundedState(2, maxThreads); // Running or Finishing
        state.setThreadBound(1, maxThreads);
    }
    
    public void setMaxThreads(int maxThreads) {
        this.maxThreads = maxThreads;
        state.setThreadBound(1, maxThreads);
    }

    public void setErrorChannel(Consumer<Throwable> channel) {
        errorChannel = channel;
    }

    public Consumer<Throwable> getErrorChannel() {
        return errorChannel;
    }

    @Override
    public void execute(Runnable command) {
        Deque<Runnable> cast = F.cast(tasks);
        cast.addFirst(command);
        update(this.maxThreads);
    }

    protected Runnable getMainBody() {
        return () -> {
            Deque<Runnable> cast = F.cast(tasks);
            while (!cast.isEmpty()) {
                Runnable last = cast.pollLast();
                if (last != null) {
                    last.run();
                }

            }
        };
    }

    protected final Runnable getRun(final int bakedMax) {
        return () -> {

            try {
                getMainBody().run();
            } catch (Throwable th) {
                try {
                    getErrorChannel().accept(th);
                } catch (Throwable err) {// we really screwed now
                    err.printStackTrace();
                }
            } finally {
                if (state.transition(0, 1)) {
                    update(bakedMax);
                    if(!state.exit(1)){
                        new IllegalStateException("Cant exit state 1").printStackTrace();
                    }
                }else{
                    if(!state.exit(0)){
                        new IllegalStateException("Cant exit state 1").printStackTrace();
                    }
                }

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
            if (!state.enter(0)) {
                new IllegalStateException("Cant enter state").printStackTrace();
            }
            getRun(maxT).run();
        } else if (maxT < 0) {//unlimited threads
            if (!state.enter(0)) {
                new IllegalStateException("Cant enter state").printStackTrace();
            }
            startThread(maxT);
        } else { // limitedThreads
            if (state.enter(0)) {
                startThread(maxT);
            }
        }
    }
}
