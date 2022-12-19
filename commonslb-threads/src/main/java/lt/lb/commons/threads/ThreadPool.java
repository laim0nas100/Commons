package lt.lb.commons.threads;

import java.lang.Thread.State;
import java.util.stream.Stream;

/**
 *
 * @author laim0nas100
 */
public interface ThreadPool {

    public Stream<Thread> enumerate(boolean recurse);

    /**
     * Interrupts thread which are of state
     * {@code TIMED_WAITING, WAITING or BLOCKED}
     */
    public default void interruptWaiting() {
        enumerate(true)
                .filter(t -> {
                    Thread.State state = t.getState();
                    return (state == State.TIMED_WAITING || state == Thread.State.WAITING || state == Thread.State.BLOCKED);
                }).forEach(Thread::interrupt);
    }

    /**
     * Create a new thread, with set name, priority, daemon and
     * {@link ThreadGroup} properties
     *
     * @param run
     * @return
     */
    public Thread createThread(Runnable run);

    /**
     * The {@link threadGroup} that this ThreadPool uses when creating threads.
     *
     * @return
     */
    public ThreadGroup getThreadGroup();

    /**
     * threads has this priority when created by ThreadPool
     *
     * @return
     */
    public int getPriority();

    public void setPriority(int priority);

    /**
     * wether ThreadPool creates daemon threads
     *
     * @return
     */
    public boolean isDaemon();

    public void setDaemon(boolean daemon);

    /**
     * ThreadPool creates threads that are started
     *
     * @return
     */
    public boolean isStarting();

    public void setStarting(boolean start);

}
