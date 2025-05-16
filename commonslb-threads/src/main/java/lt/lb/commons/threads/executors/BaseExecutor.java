package lt.lb.commons.threads.executors;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.TimeUnit;
import static java.util.concurrent.TimeUnit.NANOSECONDS;
import java.util.concurrent.TimeoutException;
import lt.lb.commons.F;

/**
 *
 * @author laim0nas100
 */
public abstract class BaseExecutor extends AbstractExecutorService implements CloseableExecutor {

    protected volatile boolean open = true;

    @Override
    public boolean isShutdown() {
        return !open;
    }

    public abstract int parallelism();

    public void executeAll(Collection<Runnable> all) {
        for (Runnable run : all) {
            execute(run);
        }
    }

    /**
     * Default jdk 19 implementation
     */
    @Override
    public void close() {
        boolean terminated = isTerminated();
        if (!terminated) {
            shutdown();
            boolean interrupted = false;
            while (!terminated) {
                try {
                    terminated = awaitTermination(1L, TimeUnit.DAYS);
                } catch (InterruptedException e) {
                    if (!interrupted) {
                        shutdownNow();
                        interrupted = true;
                    }
                }
            }
            if (interrupted) {
                Thread.currentThread().interrupt();
            }
        }
    }

    @Override
    protected <T> RunnableFuture<T> newTaskFor(Callable<T> task) {
        if (task instanceof RunnableFuture) {
            return F.cast(task);
        } else {
            return new FutureTask<>(task);
        }
    }

    @Override
    protected <T> RunnableFuture<T> newTaskFor(Runnable task, T res) {
        if (task instanceof RunnableFuture) {
            return F.cast(task);
        } else {
            return new FutureTask<>(task, res);
        }
    }

    @Override
    public <T> Future<T> submit(Callable<T> task) {
        RunnableFuture<T> t = newTaskFor(task);
        execute(t);
        return t;
    }

    @Override
    public <T> Future<T> submit(Runnable task, T result) {
        RunnableFuture<T> t = newTaskFor(task, result);
        execute(t);
        return t;
    }

    @Override
    public Future<?> submit(Runnable task) {
        RunnableFuture<?> t = newTaskFor(task, null);
        execute(t);
        return t;
    }

    public static class Running {

        public static final Running EMPTY = new Running(null, null);

        public final Thread thread;
        public volatile Runnable runnable;
        public boolean canceled;

        public Running(Thread thread, Runnable runnable) {
            this.thread = thread;
            this.runnable = runnable;
        }
    }

    /**
     * the main mechanics of invokeAny.
     */
    private <T> T doInvokeAny(Collection<? extends Callable<T>> tasks,
            boolean timed, long nanos)
            throws InterruptedException, ExecutionException, TimeoutException {
        if (tasks == null) {
            throw new NullPointerException();
        }
        int ntasks = tasks.size();
        if (ntasks == 0) {
            throw new IllegalArgumentException();
        }
        ArrayList<Future<T>> futures = new ArrayList<>(ntasks);
        ExecutorCompletionService<T> ecs
                = new ExecutorCompletionService<T>(this);

        // For efficiency, especially in executors with limited
        // parallelism, check to see if previously submitted tasks are
        // done before submitting more of them. This interleaving
        // plus the exception mechanics account for messiness of main
        // loop.
        try {
            // Record exceptions so that if we fail to obtain any
            // result, we can throw the last exception we got.
            ExecutionException ee = null;
            final long deadline = timed ? System.nanoTime() + nanos : 0L;
            Iterator<? extends Callable<T>> it = tasks.iterator();

            // Start one task for sure; the rest incrementally
            futures.add(ecs.submit(it.next()));
            --ntasks;
            int active = 1;

            for (;;) {
                Future<T> f = ecs.poll();
                if (f == null) {
                    if (ntasks > 0) {
                        --ntasks;
                        futures.add(ecs.submit(it.next()));
                        ++active;
                    } else if (active == 0) {
                        break;
                    } else if (timed) {
                        f = ecs.poll(nanos, NANOSECONDS);
                        if (f == null) {
                            throw new TimeoutException();
                        }
                        nanos = deadline - System.nanoTime();
                    } else {
                        f = ecs.take();
                    }
                }
                if (f != null) {
                    --active;
                    try {
                        return f.get();
                    } catch (ExecutionException eex) {
                        ee = eex;
                    } catch (RuntimeException rex) {
                        ee = new ExecutionException(rex);
                    }
                }
            }

            if (ee == null) {
                ee = new ExecutionException(new IllegalStateException("Failed without exception"));
            }
            throw ee;

        } finally {
            cancelAll(futures);
        }
    }

    /**
     * @throws InterruptedException {@inheritDoc}
     * @throws NullPointerException {@inheritDoc}
     * @throws IllegalArgumentException {@inheritDoc}
     * @throws ExecutionException {@inheritDoc}
     * @throws RejectedExecutionException {@inheritDoc}
     */
    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks)
            throws InterruptedException, ExecutionException {
        try {
            return doInvokeAny(tasks, false, 0);
        } catch (TimeoutException cannotHappen) {
            assert false;
            return null;
        }
    }

    /**
     * @throws InterruptedException {@inheritDoc}
     * @throws NullPointerException {@inheritDoc}
     * @throws TimeoutException {@inheritDoc}
     * @throws ExecutionException {@inheritDoc}
     * @throws RejectedExecutionException {@inheritDoc}
     */
    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks,
            long timeout, TimeUnit unit)
            throws InterruptedException, ExecutionException, TimeoutException {
        return doInvokeAny(tasks, true, unit.toNanos(timeout));
    }

    /**
     * @throws InterruptedException {@inheritDoc}
     * @throws NullPointerException {@inheritDoc}
     * @throws RejectedExecutionException {@inheritDoc}
     */
    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks)
            throws InterruptedException {
        if (tasks == null) {
            throw new NullPointerException();
        }
        ArrayList<RunnableFuture<T>> futures = new ArrayList<>(tasks.size());
        try {
            for (Callable<T> t : tasks) {
                RunnableFuture<T> f = newTaskFor(t);
                futures.add(f);
                execute(f);
            }
            for (int i = 0, size = futures.size(); i < size; i++) {
                Future<T> f = futures.get(i);
                if (!f.isDone()) {
                    try {
                        f.get();
                    } catch (CancellationException | ExecutionException ignore) {
                    }
                }
            }
            return F.cast(futures);
        } catch (Throwable t) {
            cancelAll(F.cast(futures));
            throw t;
        }
    }

    /**
     * @throws InterruptedException {@inheritDoc}
     * @throws NullPointerException {@inheritDoc}
     * @throws RejectedExecutionException {@inheritDoc}
     */
    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks,
            long timeout, TimeUnit unit)
            throws InterruptedException {
        if (tasks == null) {
            throw new NullPointerException();
        }
        final long nanos = unit.toNanos(timeout);
        final long deadline = System.nanoTime() + nanos;
        ArrayList<Future<T>> futures = new ArrayList<>(tasks.size());
        int j = 0;
        timedOut:
        try {
            for (Callable<T> t : tasks) {
                futures.add(newTaskFor(t));
            }

            final int size = futures.size();

            // Interleave time checks and calls to execute in case
            // executor doesn't have any/much parallelism.
            if (parallelism() <= 1) {
                for (int i = 0; i < size; i++) {
                    if (((i == 0) ? nanos : deadline - System.nanoTime()) <= 0L) {
                        break timedOut;
                    }
                    execute((Runnable) futures.get(i));
                }
            } else {
                executeAll(F.cast(futures));
            }

            for (; j < size; j++) {
                Future<T> f = futures.get(j);
                if (!f.isDone()) {
                    try {
                        f.get(deadline - System.nanoTime(), NANOSECONDS);
                    } catch (CancellationException | ExecutionException ignore) {
                    } catch (TimeoutException timedOut) {
                        break timedOut;
                    }
                }
            }
            return futures;
        } catch (Throwable t) {
            cancelAll(futures);
            throw t;
        }
        // Timed out before all the tasks could be completed; cancel remaining
        cancelAll(futures, j);
        return futures;
    }

    protected <T> void cancelAll(ArrayList<Future<T>> futures) {
        cancelAll(futures, 0);
    }

    /**
     * Cancels all futures with index at least j.
     */
    protected <T> void cancelAll(ArrayList<Future<T>> futures, int j) {
        for (int size = futures.size(); j < size; j++) {
            futures.get(j).cancel(true);
        }
    }
}
