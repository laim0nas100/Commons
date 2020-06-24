package lt.lb.commons.jobs;

import java.util.Iterator;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeoutException;
import lt.lb.commons.threads.sync.RepeatedRequestCollector;
import lt.lb.commons.threads.sync.WaitTime;

/**
 * Job executor with provided base executor. No cleanup is necessary. Job
 * scheduling is is also multi-threaded.
 *
 * @author laim0nas100
 */
public class JobExecutor {

    protected Executor exe;

    protected boolean isShutdown = false;
    protected final ConcurrentLinkedDeque<Job> jobs = new ConcurrentLinkedDeque<>();

    protected final JobEventListener rescanJobs = l -> addScanRequest();

    protected volatile CompletableFuture awaitJobEmpty = new CompletableFuture();

    public JobExecutor(Executor exe) {
        this(2, exe, exe);
    }

    public JobExecutor(int rescanThrottle, Executor exe, Executor rescanExecutor) {
        this.exe = exe;
        this.rrc = new RepeatedRequestCollector(rescanThrottle, () -> rescanJobs0(), rescanExecutor);
    }

    /**
     * Add job in job list. Does not become scheduled instantly.
     *
     * @param job
     */
    public void submit(Job job) {
        if (isShutdown) {
            throw new IllegalStateException("Shutdown was called");
        }
        job.addListener(SystemJobEventName.ON_DONE, rescanJobs);
        job.addListener(SystemJobEventName.ON_FAILED_TO_START, rescanJobs);
        jobs.add(job);

        addScanRequest();
    }

    protected void addScanRequest() {
        rrc.addRequest();
    }

    /**
     * Rescans after a job becomes done or a new job is submitted. Schedule
     * ready jobs and discard discardable. If no more jobs are left, completes
     * emptiness waiter.
     *
     * Doesn't run automatically. If you are using special dependencies, for
     * example "run only if current day is Christmas", it will not check every
     * day. It's the responsibility of the user to rescan periodically if such
     * dependencies are used.
     */
    public void rescanJobs() {
        this.addScanRequest();
    }

    private RepeatedRequestCollector rrc;

    private void rescanJobs0() {

        Iterator<Job> iterator = jobs.iterator();
        while (iterator.hasNext()) {
            Job job = iterator.next();
            if (job == null) {
                continue;
            }
            if (job.isDone()) {
                if (job.discarded.compareAndSet(false, true)) {
                    job.fireSystemEvent(new SystemJobEvent(SystemJobEventName.ON_DISCARDED, job));
                    iterator.remove();
                }
            } else if (job.canRun()) {
                if (job.scheduled.compareAndSet(false, true)) {
                    job.fireSystemEvent(new SystemJobEvent(SystemJobEventName.ON_SCHEDULED, job));
                    try {
                        //we dont control executor, so just in case it is bad
                        exe.execute(job.asRunnable());
                    } catch (Throwable t) {
                    }
                }
            }
        }
        if (isEmpty()) {
            this.awaitJobEmpty.complete(null);
            this.awaitJobEmpty = new CompletableFuture();
        }

    }

    /**
     *
     * @return true if no more jobs left.
     */
    public boolean isEmpty() {
        for (Job j : jobs) {
            if (j != null) {
                return false;
            }
        }
        return true;

    }

    /**
     * Only prevents new jobs from being submitted and completes termination
     * waiter.
     */
    public void shutdown() {
        this.isShutdown = true;
        this.rescanJobs();
    }

    /**
     * Wait a given time for job list to be empty
     *
     * @param time
     * @return
     * @throws InterruptedException
     */
    public boolean awaitJobEmptiness(WaitTime time)
            throws InterruptedException {
        if (isEmpty()) {
            return true;
        }
        try {
            this.awaitJobEmpty.get(time.time, time.unit);
        } catch (ExecutionException | TimeoutException ex) {
            return false;
        }
        return true;
    }

    /**
     * If shutdown was fired, then wait for job list to be empty.
     *
     * @param time
     * @return
     * @throws InterruptedException
     * @throws IllegalStateException if shutdown was not called
     */
    public boolean awaitTermination(WaitTime time)
            throws InterruptedException, IllegalStateException {
        if (!isShutdown) {
            throw new IllegalStateException("Shutdown was not called");
        }
        return awaitJobEmptiness(time);
    }

}
