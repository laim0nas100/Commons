package lt.lb.commons.threads.executors;

import java.util.Comparator;
import java.util.concurrent.PriorityBlockingQueue;
import lt.lb.commons.F;
import lt.lb.commons.Java;
import lt.lb.commons.threads.SourcedThreadPool;
import lt.lb.commons.threads.sync.ConcurrentArena;
import lt.lb.commons.threads.sync.WaitTime;

/**
 *
 * {@inheritDoc}
 *
 * Also supports priority based ordering.
 *
 * @author laim0nas100
 */
public class PriorityFastWaitingExecutor extends FastWaitingExecutor {

    private static final Comparator<Runnable> priorityComparator = (Runnable r1, Runnable r2) -> {
        if (r1 instanceof PriorityRunnable) {
            PriorityRunnable p1 = F.cast(r1);
            if (r2 instanceof PriorityRunnable) {
                PriorityRunnable p2 = F.cast(r2);
                int compare = Integer.compare(p2.order, p1.order);
                if (compare != 0) {
                    return compare;
                }
                return Long.compare(p2.time, p1.time);
            } else {
                return -1;
            }
        } else if (r2 instanceof PriorityRunnable) {
            return 1;
        }
        return 0;
    };

    private class PriorityRunnable implements Runnable {

        int order;
        long time;
        Runnable runnable;

        public PriorityRunnable(Runnable r, int order) {
            if (r == null) {
                throw new NullPointerException("Runnable is null");
            }
            this.time = Java.getNanoTime();
            this.runnable = r;
            this.order = order;
        }

        @Override
        public void run() {
            this.runnable.run();
        }
    }

    /**
     *
     * {@inheritDoc}
     */
    public PriorityFastWaitingExecutor(int maxThreads) {
        this(maxThreads, WaitTime.ofSeconds(1));
    }

    /**
     * {@inheritDoc}
     */
    public PriorityFastWaitingExecutor(int maxThreads, WaitTime time) {
        super(maxThreads, time, new SourcedThreadPool(PriorityFastWaitingExecutor.class));
        this.wt = time;
    }

    @Override
    protected ConcurrentArena<Runnable> makeQueue() {
        if (maxThreads == 0) {
            return null;
        }
        return ConcurrentArena.fromBlocking(new PriorityBlockingQueue<>(16, priorityComparator.reversed()));
    }

    /**
     * Higher priority means execute sooner. If priority below 0, executes after
     * runnables without priority
     *
     * @param priority
     * @param run
     */
    public void execute(int priority, Runnable run) {
        this.add(new PriorityRunnable(run, priority));
    }

    @Override
    public void execute(Runnable command) {
        this.execute(0, command);
    }

    private void add(Runnable run) {
        super.execute(run);
    }

}
