/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lt.lb.commons.jobs;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author laim0nas100
 */
public class JobsExecutor {

    private ExecutorService exe;

    private boolean isShutdown = false;
    private final ConcurrentLinkedDeque<Job> jobs;
    private final ExecutorService scanner = Executors.newSingleThreadExecutor();

    private JobEventListener rescanJobs;

    public JobsExecutor(ExecutorService exe) {
        jobs = new ConcurrentLinkedDeque<>();
        this.exe = exe;
        rescanJobs = l -> {
            addScanRequest();
        };
    }

    public void submit(Runnable run) {
        this.submit(Job.fromRunnable(run));
    }

    public void submit(Job job) {
        if (isShutdown) {
            throw new Error("Shutdown was called");
        }
        jobs.add(job);
        job.addListener(JobEvent.ON_DONE, rescanJobs);
        addScanRequest();
    }

    private synchronized void addScanRequest() {
        Runnable r = () -> {
            rescanJobs();
        };
        scanner.submit(r);
    }

    public void rescanJobs() {
        Iterator<Job> iterator = jobs.iterator();
        while (iterator.hasNext()) {
            Job job = iterator.next();
            if (job.isDiscardable()) {
                iterator.remove();
            } else if (job.isReady()) {
                exe.submit(job);
            }
        }
        if (isShutdown && jobs.isEmpty()) {
            exe.shutdown();
            scanner.shutdown();
        }
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

    public List<Runnable> shutdownNow() {
        return this.exe.shutdownNow();
    }

    public boolean isShutdown() {
        return this.exe.isShutdown();
    }

    public boolean isTerminated() {
        return this.exe.isTerminated();
    }

    public boolean awaitTermination(long l, TimeUnit tu) throws InterruptedException {
        return this.exe.awaitTermination(l, tu);
    }

}
