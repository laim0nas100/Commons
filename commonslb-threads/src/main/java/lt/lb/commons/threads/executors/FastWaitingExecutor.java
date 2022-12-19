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
        this(maxThreads, time, new SimpleThreadPool("WaitingFastExecutor ", new ThreadGroup("FastWaitingExecutor")));
    }

    protected FastWaitingExecutor(int maxThreads, WaitTime time, ThreadPool pool) {
        super(maxThreads, pool);
        this.tasks = new LinkedBlockingDeque<>();
        this.wt = time;
    }

    @Override
    protected Runnable getMainBody() {
        return () -> {
            LinkedBlockingDeque<Runnable> deque = F.cast(tasks);
            Runnable last = null;
            do {
                if (deque.isEmpty() && !open) {
                    break;
                }
                try {
                    last = deque.pollLast(wt.time, wt.unit);
                } catch (InterruptedException ex) {
                }
                if (last != null) {
                    last.run();
                }

            } while (!deque.isEmpty() || last != null);
        };
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
