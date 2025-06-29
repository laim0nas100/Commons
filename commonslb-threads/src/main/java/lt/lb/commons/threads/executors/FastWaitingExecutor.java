package lt.lb.commons.threads.executors;

import java.util.Objects;
import java.util.concurrent.LinkedBlockingQueue;
import lt.lb.commons.threads.SourcedThreadPool;
import lt.lb.commons.threads.sync.ConcurrentArena;
import lt.lb.commons.threads.sync.WaitTime;

/**
 *
 * Similar to @see FastExecutor, but spawns new Threads sparingly. Simply
 * waiting set time for new tasks become available before exiting. Default wait
 * time is 1 second.
 *
 * @author laim0nas100
 */
public class FastWaitingExecutor extends FastExecutor {

    protected WaitTime wt;

    public FastWaitingExecutor(int maxThreads) {
        this(maxThreads, WaitTime.ofSeconds(1));
    }

    public FastWaitingExecutor(int maxThreads, WaitTime time) {
        this(maxThreads, time, new SourcedThreadPool(FastWaitingExecutor.class));
    }

    protected FastWaitingExecutor(int maxThreads, WaitTime time, SourcedThreadPool pool) {
        super(maxThreads, pool);
        this.tasks = makeQueue();
        this.wt = Objects.requireNonNull(time);
    }

    @Override
    protected ConcurrentArena<Runnable> makeQueue() {
        if (maxThreads == 0) {
            return null;
        }
        return ConcurrentArena.fromBlocking(new LinkedBlockingQueue<>());
    }

    @Override
    protected Runnable getNext() throws InterruptedException{
        return tasks.poll(wt.time, wt.unit);
    }
    
    /**
     * {@inheritDoc} Additionally interrupts threads in wait state.
     */
    @Override
    public void close() {
        super.close();
        pool.interruptWaiting();
    }

}
