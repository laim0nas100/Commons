package lt.lb.commons.threads.executors;

import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;
import lt.lb.commons.F;
import lt.lb.commons.threads.SimpleThreadPool;
import lt.lb.commons.threads.ThreadPool;
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
        this(maxThreads, time, new SimpleThreadPool(FastWaitingExecutor.class));
    }

    protected FastWaitingExecutor(int maxThreads, WaitTime time, ThreadPool pool) {
        super(maxThreads, pool);
        this.tasks = makeQueue();
        this.wt = Objects.requireNonNull(time);
    }

    @Override
    protected Queue<Runnable> makeQueue() {
        if (maxThreads == 0) {
            return null;
        }
        return new LinkedBlockingQueue<>();
    }

    @Override
    protected void polling() {
        BlockingQueue<Runnable> queue = F.cast(tasks);
        try {
            int index = -1;
            while (!queue.isEmpty()) {

                Runnable first = queue.poll(wt.time, wt.unit);
                if (first == null) {
                    return;
                } else {
                    adds.decrementAndGet();
                }
                index = executeSingle(index, first, true);

            }
        } catch (InterruptedException ex) {
        }
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
