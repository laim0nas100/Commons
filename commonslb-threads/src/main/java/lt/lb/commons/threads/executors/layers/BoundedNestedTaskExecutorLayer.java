package lt.lb.commons.threads.executors.layers;

import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;
import lt.lb.commons.F;

/**
 *
 * {@inheritDoc} Additionally we can bound to specific amount of threads, or we
 * can turn off nested checks and just use the thread bound property.
 *
 * @author laim0nas100
 */
public class BoundedNestedTaskExecutorLayer extends NestedTaskSubmitionExecutorLayer {

    protected AtomicInteger spotsLeft;
    protected ConcurrentLinkedDeque<Runnable> list = new ConcurrentLinkedDeque<>();
    protected final boolean nestedChecks;

    public BoundedNestedTaskExecutorLayer(ExecutorService exe, int maxThreads, boolean nestedChecks) {
        super(exe);
        this.spotsLeft = new AtomicInteger(maxThreads);
        this.nestedChecks = nestedChecks;
    }

    public BoundedNestedTaskExecutorLayer(ExecutorService exe, int maxThreads) {
        this(exe, maxThreads, true);
    }

    @Override
    public void execute(Runnable run) {
        if (nestedChecks && inside.get()) {
            // submitted new task while inside a thread.
            F.checkedRun(run);
            return;

        }

        if (!list.isEmpty()) { // running behind
            list.addFirst(run); // this runnable will be delayed
            do {
                //try to execute what has been submitted before
                maybeStartNext();

            } while (!list.isEmpty() && spotsLeft.get() > 0);
            return;
        }

        if (spotsLeft.decrementAndGet() >= 0) { // borrow 1
            justSubmit(run); // increments inside
        } else {
            list.addFirst(run); // will execute later
            spotsLeft.incrementAndGet(); // compensate 1
        }

    }

    private void maybeStartNext() {
        if (list.isEmpty()) {
            return;
        }
        if (spotsLeft.decrementAndGet() >= 0) { // borrow 1
            Runnable next = list.pollLast();
            if (next == null) { // failed to get next task, compensate 1
                spotsLeft.incrementAndGet();
            } else {
                justSubmit(next); // increments inside
            }
        } else {
            spotsLeft.incrementAndGet(); // compensate 1
        }

    }

    private void justSubmit(Runnable run) {
        exe.execute(() -> { //delegate
            if (nestedChecks) {
                inside.set(true);
            }
            
            F.checkedRun(run);
            if (nestedChecks) {
                inside.set(false);
            }
            spotsLeft.incrementAndGet();
            maybeStartNext();
        });
    }

}
