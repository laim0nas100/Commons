package lt.lb.commons.threads;

import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.stream.Stream;
import lt.lb.commons.Nulls;

/**
 *
 * @author laim0nas100
 */
public class SimpleThreadPool implements ThreadPool {

    /**
     * Empty subclass to differentiate threads from the same group created by
     * this ThreadPool
     */
    public static class SimpleThread extends Thread {

        public SimpleThread(ThreadGroup group, Runnable target, String name) {
            super(group, target, name);
        }

    }

    protected boolean deamon = false;
    protected int priority = Thread.NORM_PRIORITY;
    protected boolean starting = true;
    protected String threadPrefix = "";
    protected String threadSuffix = "";
    protected AtomicLong threadNum = new AtomicLong(1L);
    protected ClassLoader loader;

    protected final ThreadGroup group;

    public SimpleThreadPool(Class cls) {
        this(cls.getSimpleName() + " ", new ThreadGroup(cls.getSimpleName()));
    }

    public SimpleThreadPool(String prefix, ThreadGroup group) {
        this.threadPrefix = Nulls.requireNonNull(prefix, "Thread prefix is null");
        this.group = Nulls.requireNonNull(group, "Threadgroup is null");
    }

    public SimpleThreadPool(String prefix) {
        this(prefix, new ThreadGroup("ThreadPool"));
    }

    @Override
    public int getPriority() {
        return priority;
    }

    @Override
    public void setPriority(int priority) {
        if (priority < Thread.MIN_PRIORITY || priority > Thread.MAX_PRIORITY) {
            return;
        }
        boolean change = this.priority != priority;
        this.priority = priority;
        if (change) {
            decorateAliveThreads(thread -> thread.setPriority(priority));
        }
    }

    @Override
    public boolean isDaemon() {
        return deamon;
    }

    @Override
    public void setDaemon(boolean deamon) {
        boolean change = deamon != this.deamon;
        this.deamon = deamon;
        if (change) {
            decorateAliveThreads(thread -> thread.setDaemon(deamon));
        }
    }

    @Override
    public boolean isStarting() {
        return starting;
    }

    @Override
    public void setStarting(boolean start) {
        this.starting = start;
    }

    public String getThreadPrefix() {
        return threadPrefix;
    }

    public void setThreadPrefix(String threadPrefix) {
        this.threadPrefix = Nulls.requireNonNull(threadPrefix, "threadPrefix must not null");
    }

    public String getThreadSuffix() {
        return threadSuffix;
    }

    public void setThreadSuffix(String threadSuffix) {
        this.threadSuffix = Nulls.requireNonNull(threadSuffix, "threadSuffix must not null");
    }

    protected void decorateAliveThreads(Consumer<Thread> consumer) {
        enumerate(false).filter(t -> t.isAlive()).forEach(consumer);
    }

    @Override
    public Stream<Thread> enumerate(boolean recurse) {
        int activeCount = group.activeCount();
        int size = activeCount + Math.max(16, activeCount / 2);// ensure none are ignored
        Thread[] threads = new Thread[size];
        group.enumerate(threads);
        return Stream.of(threads).filter(t -> (t != null && t instanceof SimpleThread));
    }

    protected String nextThreadName() {
        return threadPrefix + "thread-" + threadNum.getAndIncrement() + threadSuffix;
    }

    @Override
    public Thread newThread(Runnable run) {
        Thread thread = new SimpleThread(getThreadGroup(), Nulls.requireNonNull(run), nextThreadName());
        thread.setDaemon(isDaemon());
        thread.setPriority(getPriority());
        thread.setContextClassLoader(getContextClassLoader());
        if (isStarting()) {
            thread.start();
        }
        return thread;
    }

    @Override
    public ThreadGroup getThreadGroup() {
        return group;
    }

    @Override
    public ClassLoader getContextClassLoader() {
        return loader;
    }

    @Override
    public void setContextClassLoader(ClassLoader loader) {
        boolean change = this.loader != loader;
        this.loader = loader;
        if (change) {
            decorateAliveThreads(thread -> {
                thread.setContextClassLoader(getContextClassLoader());
            });
        }
    }

}
