package lt.lb.commons.threads.executors;

import java.util.concurrent.LinkedBlockingDeque;
import java.util.function.Predicate;
import java.util.stream.Stream;
import lt.lb.commons.F;
import lt.lb.commons.Predicates;
import lt.lb.commons.threads.sync.WaitTime;

/**
 * 
 * 
 * Similar to @see FastExecutor, but spawns new Threads sparingly. Simply waiting set
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

    @Override
    protected Thread startThread(final int maxT) {
        Thread t = super.startThread(maxT);
        t.setName("Waiting " + t.getName());
        return t;
    }

    
    /**
     * {@inheritDoc}
     * Additionally interrupts threads in wait state.
     */
    @Override
    public void close() {
        super.close();
        Thread[] threads = new Thread[tg.activeCount()];
        tg.enumerate(threads, false);
        Predicate<Thread.State> anySleeping = Predicates.anyEqual(Thread.State.TIMED_WAITING, Thread.State.WAITING);
        Predicate<Thread> anySleepingThread = Predicates.ofMapping(anySleeping, m -> m.getState());
        Stream.of(threads).filter(Predicates.isNotNull()).filter(anySleepingThread).forEach(Thread::interrupt);
    }

}
