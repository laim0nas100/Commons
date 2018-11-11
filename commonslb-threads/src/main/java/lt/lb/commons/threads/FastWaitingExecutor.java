package lt.lb.commons.threads;

import java.util.concurrent.LinkedBlockingDeque;
import lt.lb.commons.F;
import lt.lb.commons.threads.sync.WaitTime;

/**
 * Similar to FastExecutor, but spawns new Threads sparingly. Simply waiting set
 * time for new tasks become available before exiting. Default wait time is 1
 * second.
 *
 * @author laim0nas100
 */
public class FastWaitingExecutor extends FastExecutor {

    protected WaitTime wt;

    public FastWaitingExecutor(int maxThreads) {
        this(maxThreads, WaitTime.ofSeconds(1));
    }

    public FastWaitingExecutor(int maxThreads, WaitTime time) {
        super(maxThreads);
        this.tasks = new LinkedBlockingDeque<>();
        this.wt = time;
    }

    @Override
    protected Runnable getMainBody() {
        return () -> {
            LinkedBlockingDeque<Runnable> deque = F.cast(tasks);
            Runnable last = null;
            do {

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

    @Override
    protected void startThread(final int maxT) {
        Thread t = new Thread(getRun(maxT));
        t.setName("Fast Waiting Executor " + t.getName());
        t.start();
    }

}
