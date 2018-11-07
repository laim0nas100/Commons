package lt.lb.commons.threads;

import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;
import lt.lb.commons.F;
import lt.lb.commons.threads.sync.ConditionalWait;

/**
 *
 * @author laim0nas100
 */
public class EventQueue {

    public EventQueue(Executor exe) {
        this.exe = exe;
    }

    public static final String defaultTag = "DEFAULT";

    private static class Event extends FutureTask<Void> {

        public String tag = "DEFAULT";

        public Event(Callable call, String tag) {
            super(call);
            this.tag = tag;
        }

        public Event(Runnable run, String tag) {
            this(Executors.callable(run), tag);

        }
    }

    private ConcurrentLinkedDeque<Event> events = new ConcurrentLinkedDeque<>();
    private Executor exe;

    private volatile boolean shutdown = false;

    public Future add(UnsafeRunnable run) {
        return add(defaultTag, run);
    }

    public Future add(Callable call) {
        return add(defaultTag, call);
    }

    public Future add(Runnable run) {
        return add(defaultTag, run);
    }

    public Future add(String tag, UnsafeRunnable run) {
        return add(new Event(run, tag));
    }

    public Future add(String tag, Runnable run) {
        return add(new Event(run, tag));
    }

    public Future add(String tag, Callable run) {
        return add(new Event(run, tag));
    }

    private Future add(Event ev) {
        if (executing.get() && (executingThread.compareAndSet(Thread.currentThread(), Thread.currentThread()))) { // nested add
            ev.run();
            return ev;
        }

        if (shutdown) {
            throw new IllegalStateException("Is shutdown");
        }
        this.events.addLast(ev);
        executeNext();
        return ev;
    }

    private volatile ConditionalWait waiter = new ConditionalWait();
    private AtomicBoolean executing = new AtomicBoolean(false);
    private AtomicReference<Thread> executingThread = new AtomicReference<>();

    private void executeNext() {
        if (executing.compareAndSet(false, true)) {
            Runnable run = () -> {
                executingThread.set(Thread.currentThread());
                while (true) {
                    waiter.conditionalWait();
                    Event pollFirst = events.pollFirst();
                    if (pollFirst == null) {
                        if (executing.compareAndSet(true, false)) {
                            executingThread.set(null);
                        } else {
                            new IllegalStateException("How did we get here?").printStackTrace();
                            executing.set(false);
                            executeNext(); // i am immortal, unless we call shutdown
                        }
                        return;
                    } else {
                        pollFirst.run();
                    }
                }
            };
            exe.execute(run);
        }
    }

    private Predicate<Event> containsAny(String[] tags) {
        return event -> {
            for (String tag : tags) {
                if (event.tag.contains(tag)) {
                    return true;
                }
            }
            return false;
        };
    }

    public void cancelAll(String... tags) {
        waiter.requestWait();
        F.filterInPlace(events, containsAny(tags).negate());
        waiter.wakeUp();
    }

    public void shutdown() {
        shutdown = true;
    }

}
