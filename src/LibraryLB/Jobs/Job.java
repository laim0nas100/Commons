/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package LibraryLB.Jobs;

import LibraryLB.UUIDgenerator;
import LibraryLB.UUIDgenerator.ExtUUID;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Lemmin
 */
public abstract class Job implements Runnable {

    public static Job fromRunnable(Runnable run) {
        Job job = new Job() {
            @Override
            protected void logic() throws Exception {
                run.run();
            }
        };
        return job;
    }

    private Collection<JobDependency> doBefore = new HashSet<>();
    private Collection<JobDependency> doAfter = new HashSet<>();
    private ExtUUID uuid;

    private Map<String, Collection<JobEventListener>> listeners = new HashMap<>();

    private boolean canceled = false;
    private boolean failed = false;
    private boolean successfull = false;
    private boolean running = false;

    private int leftToRun;

    private Job me = this;

    private Job canceledRoot;

    public Job() {
        uuid = UUIDgenerator.nextUUID(this.getClass().hashCode());
        leftToRun = 1;
    }

    public Job(int timesToRun) {
        this();
        this.leftToRun = timesToRun;
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

    public ExtUUID getUUID() {
        return this.uuid;
    }

    public void cancel() {
        this.cancel(true);
    }

    public void cancel(boolean propogate) {
        if (this.canceled) {
            return;
        }
        this.canceled = true;
        this.fireEvent(new JobEvent(JobEvent.ON_CANCEL, me));
        if (!propogate) {
            return;
        }
        for (JobDependency j : this.doAfter) {
            j.getJob().canceledRoot = me;
            j.getJob().cancel();

        }
    }

    public boolean isReady() {

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

    public boolean isDiscardable() {
        if (this.isCanceled() || this.isFailed()) {
            return true;
        }
        return this.leftToRun == 0;
    }

    public boolean isCanceled() {
        return canceled;
    }

    public boolean isSuccessfull() {
        return successfull;
    }

    public boolean isFailed() {
        return failed;
    }

    public List<Job> getCanceledChain() {

        LinkedList<Job> chain = new LinkedList<>();
        if (!isCanceled()) {
            return chain;
        } else {
            chain.addLast(me);
        }
        if (this.canceledRoot != null) {
            chain.addAll(canceledRoot.getCanceledChain());
        }
        return chain;

    }

    public boolean isDone() {
        return ((isCanceled() || isFailed()) || isSuccessfull());
    }

    public void addBackward(String onEvent, Job job) {
        DefaultJobDependency jobd1 = new DefaultJobDependency(job, onEvent);
        this.doBefore.add(jobd1);
        DefaultJobDependency jobd2 = new DefaultJobDependency(me, onEvent);
        job.doAfter.add(jobd2);
    }

    public void addForward(String onEvent, Job job) {
        DefaultJobDependency jobd1 = new DefaultJobDependency(job, onEvent);
        this.doAfter.add(jobd1);
        DefaultJobDependency jobd2 = new DefaultJobDependency(me, onEvent);
        job.doBefore.add(jobd2);
    }

    public void addBackward(Job job) {
        this.addBackward(JobEvent.ON_SUCCEEDED, job);
    }

    public void addForward(Job job) {
        this.addForward(JobEvent.ON_SUCCEEDED, job);
    }

    protected abstract void logic() throws Exception;

    @Override
    public void run() {
        if (this.running) {
            return;
        }
        if (!this.isReady()) {
            return;
        }

        this.running = true;
        if (leftToRun > 0) {
            this.leftToRun--;
        }
        Exception e = null;
        try {
            logic();
            this.successfull = true;

        } catch (Exception ex) {
            this.failed = true;
            e = ex;
        } finally {
            if (isSuccessfull()) {
                this.fireEvent(new JobEvent(JobEvent.ON_SUCCEEDED, me));
            }
            if (isFailed()) {
                this.fireEvent(new JobEvent(JobEvent.ON_FAILED, me, e));
            }
            this.fireEvent(new JobEvent(JobEvent.ON_DONE, me));
            if (this.isDiscardable()) {
                this.fireEvent(new JobEvent(JobEvent.ON_FINISHED, me));
            }
            this.running = false;
        }
    }

    public void addListener(String name, JobEventListener listener) {
        Collection<JobEventListener> collection;
        if (!this.listeners.containsKey(name)) {
            collection = new LinkedList<>();
            this.listeners.put(name, collection);
        } else {
            collection = this.listeners.get(name);
        }
        collection.add(listener);
    }

    public void fireEvent(JobEvent event) {
        if (this.listeners.containsKey(event.getEventName())) {
            for (JobEventListener listener : this.listeners.get(event.getEventName())) {
                listener.onEvent(event);
            }
        }
    }
}
