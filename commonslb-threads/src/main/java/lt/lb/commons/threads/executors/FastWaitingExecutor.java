package lt.lb.commons.threads.executors;

import java.util.concurrent.LinkedBlockingDeque;
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
        this.tasks = new LinkedBlockingDeque<>();
        this.wt = time;
    }

    @Override
    protected void polling() {
        LinkedBlockingDeque<Runnable> deque = F.cast(tasks);
        while (open && !deque.isEmpty()) {
            try {
                Runnable last = deque.pollFirst(wt.time, wt.unit);
                executeSingle(last, true);
            } catch (InterruptedException ex) {
            }
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
