package lt.lb.commons.threads;

import java.util.concurrent.PriorityBlockingQueue;
import lt.lb.commons.F;
import lt.lb.commons.JavaProperties;
import lt.lb.commons.Timer;
import lt.lb.commons.misc.ExtComparator;
import lt.lb.commons.threads.sync.WaitTime;

/**
 *
 * {@inheritDoc} Also supports
 *
 * @author laim0nas100
 */
public class PriorityFastWaitingExecutor extends FastWaitingExecutor {

    private ExtComparator<Runnable> priorityComparator = (Runnable r1, Runnable r2) -> {
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
        Runnable ru;

        public PriorityRunnable(Runnable r, int order) {
            if (r == null) {
                throw new NullPointerException("Runnable is null");
            }
            this.time = JavaProperties.getNanoTime();
            this.ru = r;
            this.order = order;
        }

        @Override
        public void run() {
            this.ru.run();
        }
    }

    /**
     *
     * @inheritDoc
     */
    public PriorityFastWaitingExecutor(int maxThreads) {
        this(maxThreads, WaitTime.ofSeconds(1));
    }

    /**
     * @inheritDoc
     */
    public PriorityFastWaitingExecutor(int maxThreads, WaitTime time) {
        super(maxThreads);
        this.tasks = new PriorityBlockingQueue<>(1, priorityComparator.reversed());
        this.wt = time;
    }

    @Override
    protected Runnable getMainBody() {
        return () -> {
            PriorityBlockingQueue<Runnable> deque = F.cast(tasks);
            Runnable last = null;
            do {

                if (deque.isEmpty() && !open) {
                    break;
                }
                try {

                    last = deque.poll(wt.time, wt.unit);
                } catch (InterruptedException ex) {
                }
                if (last != null) {
                    last.run();
                }

            } while (!deque.isEmpty() || last != null);
        };
    }

    @Override
    protected Thread startThread(final int maxT) {
        Thread t = super.startThread(maxT);
        t.setName("Priority " + t.getName());
        return t;
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
        tasks.add(run);
        update(this.maxThreads);
    }

}
