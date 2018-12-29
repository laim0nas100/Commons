package lt.lb.commons.threads.sync;

import lt.lb.commons.threads.sync.BoundedNestedTaskExecutorLayer;
import java.util.Iterator;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import lt.lb.commons.F;
import lt.lb.commons.threads.UnsafeRunnable;

/**
 *
 * @author laim0nas100
 */
public class EventQueue {
    
    private Executor nested;
    
    private int autoCleanUpAfter = 100;
    
    public EventQueue(Executor exe) {
        nested = new BoundedNestedTaskExecutorLayer(exe, 1);
    }
    
    public static final String defaultTag = "DEFAULT";
    
    private static class Event extends FutureTask<Void>{
        
        public String tag = "DEFAULT";
        
        public Event(Callable call, String tag) {
            super(call);
            this.tag = tag;
        }
        
        public Event(Runnable run, String tag) {
            this(Executors.callable(run), tag);
        }
        
        public void run(){
            System.err.println("Executing event with tag:"+tag+ " and is done "+this.isDone());
            super.run();
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
        return add(new Event(run, tag));
    }
    
    public Future add(String tag, Runnable run) {
        return add(new Event(run, tag));
    }
    
    public Future add(String tag, Callable run) {
        return add(new Event(run, tag));
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
            F.filterInPlace(events, e -> !e.isDone());
        }
        return ev;
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
        Iterator<Event> it = events.iterator();
        Predicate<Event> containsAny = containsAny(tags);
        while (it.hasNext()) {
            Event next = it.next();
            if (containsAny.test(next)) {
                next.cancel(false);
                it.remove();
            }
        }
    }
    
    public void shutdown() {
        shutdown = true;
        events.forEach(ev ->{
            ev.cancel(false);
        });
        events.clear();
        
    }
    
}
