package lt.lb.commons.jobs;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import lt.lb.commons.F;
import lt.lb.commons.func.unchecked.UnsafeSupplier;
import lt.lb.commons.threads.Futures;

/**
 *
 * 
 * 
 * @author laim0nas100
 */
public final class Job<T> implements Future<T> {

    Collection<JobDependency<?>> doBefore = new HashSet<>();
    Collection<JobDependency<?>> doAfter = new HashSet<>();
    private final String uuid;

    Map<String, Collection<JobEventListener>> listeners = new HashMap<>();

    AtomicBoolean cancelled = new AtomicBoolean(false);
    private boolean failed = false;
    private boolean successfull = false;
    AtomicInteger failedToStart = new AtomicInteger(0);
    AtomicBoolean scheduled = new AtomicBoolean(false);
    AtomicBoolean running = new AtomicBoolean(false);

    private Job canceledParent;
    private Job canceledRoot;

    private final FutureTask<T> task;

    public Job(String uuid, UnsafeSupplier<T> call) {
        this.uuid = uuid;
        task = Futures.ofCallable(call);
    }

    public Job(UnsafeSupplier<T> run) {
        this(UUID.randomUUID().toString(), run);
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
        this.fireEvent(new JobEvent(JobEvent.ON_CANCEL, this));
        boolean ok = task.cancel(interrupt);
        if (propogate) {
            for (JobDependency j : this.doAfter) {
                j.getJob().canceledRoot = root;
                j.getJob().canceledParent = this;
                j.getJob().cancelInner(interrupt, propogate, root);

            }
        }
        return ok;
    }

    public boolean cancel(boolean interrupt) {
        return this.cancel(interrupt, true);
    }

    public boolean canRun() {

        if (this.isDiscardable()) {
            return false;
        }
        for (JobDependency job : this.doBefore) {
            if (!job.isCompleted()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean isCancelled() {
        return cancelled.get();
    }

    public boolean isDiscardable() {
        return this.isDone();
    }

    public boolean isSuccessfull() {
        return successfull;
    }

    public boolean isFailed() {
        return failed;
    }

    public boolean isRunning() {
        return running.get();
    }

    public boolean isScheduled() {
        return scheduled.get();
    }
    
    public int getFailedToStart(){
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

    public boolean isDone() {
        return ((isCancelled() || isFailed()) || isSuccessfull());
    }

    public Job addBackward(String onEvent, Job job) {
        job.addDependencyAfter(new DefaultJobDependency(this, onEvent));
        this.addDependencyBefore(new DefaultJobDependency(job, onEvent));
        return this;
    }

    public Job addForward(String onEvent, Job job) {
        this.addDependencyAfter(new DefaultJobDependency(job, onEvent));
        job.addDependencyBefore(new DefaultJobDependency(this, onEvent));
        return this;
    }

    public Job addBackward(Job job) {
        return this.addBackward(JobEvent.ON_SUCCEEDED, job);
    }

    public Job addForward(Job job) {
        return this.addForward(JobEvent.ON_SUCCEEDED, job);
    }

    public Job addDependencyBefore(JobDependency dep) {
        if (this.isScheduled()) {
            throw new IllegalStateException("Job has been scheduled, dependencies should not change");
        }
        this.doBefore.add(dep);
        return this;
    }

    public Job addDependencyAfter(JobDependency dep) {
        if (this.isScheduled()) {
            throw new IllegalStateException("Job has been scheduled, dependencies should not change");
        }
        this.doAfter.add(dep);
        return this;
    }

    public void run() {
        if (!this.canRun()) {
            this.fireEvent(new JobEvent(JobEvent.ON_FAILED_TO_START, this));
            this.failedToStart.incrementAndGet();
            this.scheduled.set(false);
            return;
        }
        if (this.running.compareAndSet(false, true)) { // ensure only one running instance

            this.fireEvent(new JobEvent(JobEvent.ON_EXECUTE, this));
            task.run();
            Optional<Throwable> error = F.checkedRun(task::get);
            if (error.isPresent()) {
                this.failed = true;
            } else {
                this.successfull = true;
            }
            if (isSuccessfull()) {
                this.fireEvent(new JobEvent(JobEvent.ON_SUCCEEDED, this));
            }
            if (isFailed() && error.isPresent()) {
                this.fireEvent(new JobEvent(JobEvent.ON_FAILED, this, error.get()));
            }
            this.fireEvent(new JobEvent(JobEvent.ON_DONE, this));
            if (this.isDiscardable()) {
                this.fireEvent(new JobEvent(JobEvent.ON_BECAME_DISCARDABLE, this));
            }

            if (!this.running.compareAndSet(true, false)) {
                throw new IllegalStateException("After job:" + this.getUUID() + " ran, property running was set to false");
            }

        }

    }

    public void addListener(String nathis, JobEventListener listener) {
        listeners.computeIfAbsent(nathis, n -> new LinkedList<>()).add(listener);
    }

    public void fireEvent(JobEvent event) {
        if (this.listeners.containsKey(event.getEventName())) {
            for (JobEventListener listener : this.listeners.get(event.getEventName())) {
                F.checkedRun(() -> {
                    listener.onEvent(event);
                });
                //ignore exceptions

            }
        }
    }

    public Runnable asRunnable() {
        return this::run;
    }
}
