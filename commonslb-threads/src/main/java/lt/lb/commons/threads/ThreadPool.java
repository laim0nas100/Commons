package lt.lb.commons.threads;

import java.util.concurrent.ThreadFactory;
import java.util.stream.Stream;

/**
 *
 * @author laim0nas100
 */
public interface ThreadPool extends ThreadFactory {

    public Stream<Thread> enumerate(boolean recurse);

    /**
     * True only if thread sate is TIMED_WAITING,WAITING or BLOCKED.
     *
     * @param thread
     * @return
     */
    public static boolean threadIsWaiting(Thread thread) {
        if (thread == null) {
            return false;
        }
        switch (thread.getState()) {
            case TIMED_WAITING:
            case WAITING:
            case BLOCKED:
                return true;
            default:
                return false;
        }

    }

    /**
     * Interrupts thread which are of state
     * {@code TIMED_WAITING, WAITING or BLOCKED}
     */
    public default void interruptWaiting() {
        enumerate(true).filter(t -> threadIsWaiting(t)).forEach(Thread::interrupt);
    }

    /**
     * @{inheritDoc}
     */
    @Override
    public Thread newThread(Runnable r);

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
    
    public ClassLoader getContextClassLoader();
    
    public void setContextClassLoader(ClassLoader loader);

}
