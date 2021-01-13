package lt.lb.commons.threads.executors.layers;

import java.util.Objects;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;
import lt.lb.commons.F;

/**
 *
 * {@inheritDoc}. We delegate nested runnable call to other executor, which can
 * ignore that call if it's configured as such;
 *
 * @author laim0nas100
 */
public class DelegatingNestedTaskExecutorLayer extends NestedTaskSubmitionExecutorLayer {
    
    protected ConcurrentLinkedDeque<Runnable> list = new ConcurrentLinkedDeque<>();
    protected Executor delegated = run -> {
    };
    
    public DelegatingNestedTaskExecutorLayer(Executor exe, Executor delegated) {
        super(exe);
        this.delegated = Objects.requireNonNull(delegated);
    }
    
    public DelegatingNestedTaskExecutorLayer(Executor exe) {
        super(exe);
        delegated = run -> { // empty;
        };
    }
    
    @Override
    public void execute(Runnable run) {
        if (inside.get()) {
            // submitted new task while inside a thread.
            delegated.execute(run);
            
        } else {
            super.execute(run);
        }
        
    }
}
