package lt.lb.commons.threads.sync;

import java.util.Iterator;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Predicate;
import lt.lb.commons.F;
import lt.lb.commons.PosEq;
import lt.lb.commons.func.unchecked.UnsafeRunnable;
import lt.lb.commons.parsing.StringOp;
import lt.lb.commons.threads.executors.layers.BoundedNestedTaskExecutorLayer;

/**
 *
 * @author laim0nas100
 */
public class EventQueue {

    private Executor nested;
    public boolean preventRunningTagCancel = true;
    public boolean preventRunningSelfTagCancel = true;
    private ConcurrentLinkedDeque<Event> running = new ConcurrentLinkedDeque<>();
    private ThreadLocal<ConcurrentLinkedDeque<Event>> runningLocal = ThreadLocal.withInitial(() -> new ConcurrentLinkedDeque<>());
    public Consumer<Event> eventCallbackBefore = ev -> {
    };
    public Consumer<Event> eventCallbackAfter = ev -> {
    };

    private int autoCleanUpAfter = 100;

    public EventQueue(Executor exe) {
        nested = new BoundedNestedTaskExecutorLayer(exe, 1);
    }

    public static final String defaultTag = "DEFAULT";

    public static class Event extends FutureTask<Void> {

        public final String[] tags;
        public final EventQueue queue;

        public Event(EventQueue q, Callable call, String[] tag) {
            super(call);
            this.queue = q;
            this.tags = tag;
        }

        public Event(EventQueue q, Runnable run, String[] tag) {
            this(q, Executors.callable(run), tag);
        }

        public Event(EventQueue q, UnsafeRunnable run, String[] tag) {
            this(q, UnsafeRunnable.toCallable(run), tag);
        }

        @Override
        public void run() {
            queue.running.add(this);
            ConcurrentLinkedDeque<Event> local = queue.runningLocal.get();
            local.add(this);
            F.checkedRun(() -> {
                queue.eventCallbackBefore.accept(this);
            });
            super.run();
            F.checkedRun(() -> {
                queue.eventCallbackAfter.accept(this);
            });
            local.remove(this);
            queue.running.remove(this);
        }
    }

    private ConcurrentLinkedDeque<Event> events = new ConcurrentLinkedDeque<>();

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
        return add(new Event(this, run, StringOp.split(tag, ",")));
    }

    public Future add(String tag, Runnable run) {
        return add(new Event(this, run, StringOp.split(tag, ",")));
    }

    public Future add(String tag, Callable run) {
        return add(new Event(this, run, StringOp.split(tag, ",")));
    }

    private AtomicInteger ai = new AtomicInteger(1);

    private Future add(Event ev) {
        if (shutdown) {
            throw new IllegalStateException("Is shutdown");
        }
        events.add(ev);
        nested.execute(ev);
        if (ai.getAndIncrement() % autoCleanUpAfter == 0) {
            ai.set(1);
            Iterator<Event> iterator = events.iterator();
            while (iterator.hasNext()) {
                Event next = iterator.next();
                if (next.isDone()) {
                    iterator.remove();
                }
            }
        }
        return ev;
    }

    private Predicate<Event> containsAny(String[] tags) {
        return event -> {
            if (event.tags == null || event.tags.length == 0) {
                return false;
            }
            for (String tag : tags) {
                for (String evTag : event.tags) {
                    if (Objects.equals(tag, evTag)) {
                        return true;
                    }
                }
            }
            return false;
        };
    }

    public void dequeueAll(String... tags) {
        this.cancelAll(false, tags);
    }

    public void cancelAll(String... tags) {
        this.cancelAll(true, tags);
    }

    private boolean runningContainsTag(Iterable<Event> runningEvents, Predicate<Event> containsAny) {
        for (Event ev : runningEvents) {
            if (containsAny.test(ev)) {
                return true;
            }
        }
        return false;
    }

    public void cancelAll(boolean interupt, String... tags) {
        Iterator<Event> it = events.iterator();
        Predicate<Event> containsAny = containsAny(tags);
        while (it.hasNext()) {
            Event next = it.next();
            if (preventRunningTagCancel && runningContainsTag(running, containsAny)) {
                continue;
            }
            if (preventRunningSelfTagCancel && runningContainsTag(runningLocal.get(), containsAny)) {
                continue;
            }
            if (containsAny.test(next)) {
                next.cancel(interupt);
                it.remove();
            }
        }
    }

    public void forceShutdown() {
        shutdown = true;
        events.forEach(ev -> {
            ev.cancel(true);
        });
        events.clear();

    }

    public void shutdown() {
        shutdown = true;
        events.forEach(ev -> {
            ev.cancel(false);
        });
        events.clear();

    }

}
