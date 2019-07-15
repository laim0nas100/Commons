package lt.lb.commons.jobs;

import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 *
 * @author laim0nas100
 */
public class JobExecutor {

    protected Executor exe;

    protected boolean isShutdown = false;
    protected final ConcurrentLinkedDeque<Job> jobs = new ConcurrentLinkedDeque<>();
    protected final ExecutorService scanner = Executors.newSingleThreadExecutor();

    protected final JobEventListener rescanJobs = l -> addScanRequest();

    public JobExecutor(Executor exe) {
        this.exe = exe;
    }

    public void submit(Job job) {
        if (isShutdown) {
            throw new IllegalStateException("Shutdown was called");
        }
        jobs.add(job);
        job.addListener(JobEvent.ON_DONE, rescanJobs);
        addScanRequest();
    }

    protected void addScanRequest() {
        scanner.execute(this::rescanJobs);
    }

    public void rescanJobs() {
        Iterator<Job> iterator = jobs.iterator();
        while (iterator.hasNext()) {
            Job job = iterator.next();
            if (job == null) {
                continue;
            }
            if (job.isDiscardable()) {
                iterator.remove();
            } else if (job.canRun()) {
                if (job.scheduled.compareAndSet(false, true)) {
                    exe.execute(() -> {
                        job.fireEvent(new JobEvent(JobEvent.ON_SCHEDULED, job));
                    });

                    exe.execute(job.asRunnable());
                }

            }
        }
        if (isShutdown && jobs.isEmpty()) {
            onShutdown();
        }
    }

    protected void onShutdown() {
        scanner.shutdown();
    }

    public boolean isEmpty() {
        for (Job j : jobs) {
            if (j != null) {
                return false;
            }
        }
        return true;

    }

    public void shutdown() {
        this.isShutdown = true;
        this.rescanJobs();
    }

}
