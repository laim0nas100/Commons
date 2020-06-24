package lt.lb.commons.jobs;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import lt.lb.commons.EmptyImmutableList;
import lt.lb.commons.F;
import lt.lb.commons.func.unchecked.UnsafeConsumer;
import lt.lb.commons.func.unchecked.UnsafeFunction;
import lt.lb.commons.func.unchecked.UnsafeSupplier;
import lt.lb.commons.threads.Futures;

/**
 *
 *
 *
 * @author laim0nas100
 * @param <T>
 */
public class Job<T> implements Future<T> {

    Collection<Dependency> doBefore = new HashSet<>();
    Collection<Job> doAfter = new HashSet<>();
    final String uuid;

    Map<String, Collection<JobEventListener>> listeners = new HashMap<>();
    EnumMap<SystemJobEventName, Collection<JobEventListener>> systemListeners = new EnumMap<>(SystemJobEventName.class);
    AtomicBoolean cancelled = new AtomicBoolean(false);
    boolean failed = false;
    boolean successfull = false;
    AtomicInteger failedToStart = new AtomicInteger(0);
    AtomicBoolean scheduled = new AtomicBoolean(false);
    AtomicBoolean discarded = new AtomicBoolean(false);
    AtomicBoolean running = new AtomicBoolean(false);

    Job canceledParent;
    Job canceledRoot;

    final FutureTask<T> task;

    public Job(String uuid, UnsafeConsumer<? super Job<T>> call) {
        this.uuid = uuid;
        task = new FutureTask<>(() -> call.accept(this), null);
    }

    public Job(String uuid, UnsafeFunction<? super Job<T>, ? extends T> call) {
        this.uuid = uuid;
        UnsafeSupplier<T> sup = () -> call.applyUnsafe(this);
        task = Futures.ofCallable(sup);

    }

    public Job(UnsafeConsumer<? super Job<T>> call) {
        this(UUID.randomUUID().toString(), call);
    }

    public Job(UnsafeFunction<? super Job<T>, ? extends T> call) {
        this(UUID.randomUUID().toString(), call);
    }

    @Override
    public T get() throws InterruptedException, ExecutionException {
        return task.get();
    }

    @Override
    public T get(long time, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        return task.get(time, unit);
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 67 * hash + uuid.hashCode();
        return hash;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (o instanceof Job) {
            Job other = (Job) o;
            return other.uuid.equals(this.uuid);
        }
        return false;
    }

    public String getUUID() {
        return this.uuid;
    }

    public void cancel() {
        this.cancel(true, true);
    }

    public boolean cancel(boolean interrupt, boolean propogate) {
        return cancelInner(interrupt, propogate, this);
    }

    private boolean cancelInner(boolean interrupt, boolean propogate, Job root) {

        if (!cancelled.compareAndSet(false, true)) {
            return false;
        }
        this.fireEvent(new SystemJobEvent(SystemJobEventName.ON_CANCEL, this));
        boolean ok = task.cancel(interrupt);
        if (propogate) {
            for (Job j : this.doAfter) {
                j.canceledRoot = root;
                j.canceledParent = this;
                j.cancelInner(interrupt, propogate, root);

            }
        }
        return ok;
    }

    @Override
    public boolean cancel(boolean interrupt) {
        return this.cancel(interrupt, true);
    }

    public boolean canRun() {

        if (this.isDone()) {
            return false;
        }
        for (Dependency dep : this.doBefore) {
            if (!dep.isCompleted()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean isCancelled() {
        return cancelled.get();
    }

    public boolean isSuccessfull() {
        return successfull;
    }

    public boolean isFailed() {
        return failed;
    }

    public boolean isDiscarded() {
        return discarded.get();
    }

    public boolean isRunning() {
        return running.get();
    }

    public boolean isScheduled() {
        return scheduled.get();
    }

    public int getFailedToStart() {
        return this.failedToStart.get();
    }

    public List<Job> getCanceledChain() {

        LinkedList<Job> chain = new LinkedList<>();
        if (!isCancelled()) {
            return chain;
        } else {
            chain.addLast(this);
        }
        if (this.canceledParent != null) {
            chain.addAll(canceledParent.getCanceledChain());
        }
        return chain;

    }

    public Optional<Job> getCanceledRoot() {
        return Optional.ofNullable(this.canceledRoot);
    }

    @Override
    public boolean isDone() {
        return ((isCancelled() || isFailed()) || isSuccessfull());
    }

    /**
     * Chain jobs on given event type. Given job must execute before this.
     *
     * @param onEvent
     * @param job
     * @return
     */
    public Job chainBackward(SystemJobEventName onEvent, Job job) {
        job.addAfter(this);
        this.addDependency(new SystemJobDependency(job, onEvent));
        return this;
    }

    /**
     * Chain jobs on given event type. Given job must execute after this.
     *
     * @param onEvent
     * @param job
     * @return
     */
    public Job chainForward(SystemJobEventName onEvent, Job job) {
        this.addAfter(job);
        job.addDependency(new SystemJobDependency(this, onEvent));
        return this;
    }

    /**
     * Chain jobs on success. Given job must execute before this.
     *
     * @param job
     * @return
     */
    public Job chainBackward(Job job) {
        return this.chainBackward(SystemJobEventName.ON_SUCCESSFUL, job);
    }

    /**
     * Chain jobs on success. Given job must execute after this.
     *
     * @param job
     * @return
     */
    public Job chainForward(Job job) {
        return this.chainForward(SystemJobEventName.ON_SUCCESSFUL, job);
    }

    /**
     * Add dependency manually
     *
     * @param dep
     * @return
     */
    public Job addDependency(Dependency dep) {
        assertNoChange("dependencies");
        this.doBefore.add(dep);
        return this;
    }

    /**
     * Add job, that can be canceled via propogation, if this job is canceled
     *
     * @param dep
     * @return
     */
    public Job addAfter(Job dep) {
        assertNoChange("child jobs");
        this.doAfter.add(dep);
        return this;
    }

    /**
     * Run job manually
     */
    public void run() {
        if (!this.canRun()) {
            this.fireEvent(new SystemJobEvent(SystemJobEventName.ON_FAILED_TO_START, this));
            this.failedToStart.incrementAndGet();
            this.scheduled.set(false);
            return;
        }
        if (this.running.compareAndSet(false, true)) { // ensure only one running instance

            this.fireEvent(new SystemJobEvent(SystemJobEventName.ON_EXECUTE, this));
            task.run();
            Optional<Throwable> error = F.checkedRun(task::get);
            if (error.isPresent()) {
                this.failed = true;
            } else {
                this.successfull = true;
            }
            if (isSuccessfull()) {
                this.fireEvent(new SystemJobEvent(SystemJobEventName.ON_SUCCESSFUL, this));
            }
            if (isFailed() && error.isPresent()) {
                this.fireEvent(new SystemJobEvent(SystemJobEventName.ON_FAILED, this, error.get()));
            }
            this.fireEvent(new SystemJobEvent(SystemJobEventName.ON_DONE, this));

            if (!this.running.compareAndSet(true, false)) {
                throw new IllegalStateException("After job:" + this.getUUID() + " ran, property running was set to false");
            }

        }

    }

    /**
     * Add custom job listener
     *
     * @param name
     * @param listener
     */
    public void addListener(String name, JobEventListener listener) {
        assertNoChange("listeners");
        listeners.computeIfAbsent(name, n -> new LinkedList<>()).add(listener);
    }

    /**
     * Add system job listener
     *
     * @param name
     * @param listener
     */
    public void addListener(SystemJobEventName name, JobEventListener listener) {
        assertNoChange("systemListeners");
        systemListeners.computeIfAbsent(name, n -> new LinkedList<>()).add(listener);
    }

    private void assertNoChange(String msg) {
        if (this.isScheduled()) {
            throw new IllegalStateException("Job has been scheduled, " + msg + " should not change");
        }
    }

    public void fireEvent(JobEvent event) {

        Objects.requireNonNull(event);
        Collection<JobEventListener> collection;
        if (event instanceof SystemJobEvent) {
            SystemJobEvent sysEvent = (SystemJobEvent) event;
            collection = this.systemListeners.getOrDefault(sysEvent.enumName, EmptyImmutableList.getInstance());
        } else {
            collection = this.listeners.getOrDefault(event.getEventName(), EmptyImmutableList.getInstance());
        }
        for (JobEventListener listener : collection) {
            F.checkedRun(() -> {
                listener.onEvent(event);
            });
            //ignore exceptions, because those should've been handled inside event listeners

        }
    }

    public Runnable asRunnable() {
        return this::run;
    }
}
