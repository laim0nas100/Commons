package lt.lb.commons.jobs;

import java.util.Iterator;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeoutException;
import lt.lb.commons.threads.sync.WaitTime;

/**
 * Job executor with provided base executor. No cleanup is necessary. Job
 * scheduling is performed in the same executor as job execution and is also
 * multi-threaded.
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
        this.exe = exe;
    }

    /**
     * Add job in job list. Not necessary becomes scheduled instantly.
     *
     * @param job
     */
    public void submit(Job job) {
        if (isShutdown) {
            throw new IllegalStateException("Shutdown was called");
        }
        jobs.add(job);
        job.addListener(JobEvent.ON_DONE, rescanJobs);
        addScanRequest();
    }

    protected void addScanRequest() {
        exe.execute(this::rescanJobs);
    }

    /**
     * Rescan jobs. Schedule ready jobs and discard discardable. If no more jobs
     * are left, completes emptiness waiter.
     */
    public void rescanJobs() {
        Iterator<Job> iterator = jobs.iterator();
        while (iterator.hasNext()) {
            Job job = iterator.next();
            if (job == null) {
                continue;
            }
            if (job.isDone()) {
                if (job.discarded.compareAndSet(false, true)) {
                    iterator.remove();
                }
            } else if (job.canRun()) {
                if (job.scheduled.compareAndSet(false, true)) {
                    job.fireEvent(new JobEvent(JobEvent.ON_SCHEDULED, job));

                    exe.execute(job.asRunnable());
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
     * Only prevents new jobs from being submitted and completes termination waiter.
     */
    public void shutdown() {
        this.isShutdown = true;
        this.rescanJobs();
    }

    /**
     * Wait a given time for job list to be empty
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
        } catch (ExecutionException ex) {
        } catch (TimeoutException ex) {
            return false;
        }
        return true;
    }

    /**
     * If shutdown was fired, then wait for job list to be empty.
     * @param time
     * @return
     * @throws InterruptedException 
     * @throws IllegalStateException if shutdown was not called
     */
    public boolean awaitTermination(WaitTime time)
            throws InterruptedException,IllegalStateException {
        if (!isShutdown) {
            throw new IllegalStateException("Shutdown was not called");
        }
        return awaitJobEmptiness(time);
    }

}
