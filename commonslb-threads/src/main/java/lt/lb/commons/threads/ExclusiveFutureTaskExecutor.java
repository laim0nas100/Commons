package lt.lb.commons.threads;

import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import lt.lb.commons.Nulls;

/**
 *
 * @author laim0nas100
 */
public class ExclusiveFutureTaskExecutor<T> {

    protected Executor executor;
    protected AtomicBoolean running = new AtomicBoolean(false);
    protected AtomicReference<Future<T>> reference = new AtomicReference<>();

    public ExclusiveFutureTaskExecutor(Executor executor) {
        this.executor = Nulls.requireNonNull(executor);
    }

    public static <V> Future<V> execute(boolean auto, AtomicBoolean running, AtomicReference<Future<V>> reference, Callable<V> task, Executor executor) {
        if (running.get()) {
            return reference.get();
        }
        if (reference.get() == null || auto) {
            FutureTask<V> newTask = new FutureTask<>(realCallable(reference, running, task));
            reference.set(newTask);
            executor.execute(newTask);
            return newTask;
        }
        return reference.get();
    }

    public Future<T> invalidate() {
        return reference.getAndSet(null);
    }

    public Future<T> execute(boolean auto, Callable<T> call) {
        return execute(auto, running, reference, call, executor);
    }
    
    public Future<T> execute(Callable<T> call) {
        return execute(true, running, reference, call, executor);
    } 

    private static <V> Callable<V> realCallable(AtomicReference<Future<V>> reference, AtomicBoolean running, Callable<V> task) {
        return () -> {
            if (running.compareAndSet(false, true)) {
                try {
                    return task.call();
                } finally {
                    running.set(false);
                }

            }
            return reference.get().get();
        };
    }

}
