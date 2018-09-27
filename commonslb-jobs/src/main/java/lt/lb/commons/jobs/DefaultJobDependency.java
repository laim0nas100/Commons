/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lt.lb.commons.jobs;

import java.util.Objects;

/**
 *
 * @author laim0nas100
 */
public class DefaultJobDependency implements JobDependency {

    private Job job;
    private String onEvent;

    public DefaultJobDependency(Job j, String event) {
        job = j;
        onEvent = event;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (o instanceof DefaultJobDependency) {
            return this.hashCode() == o.hashCode();
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 83 * hash + this.job.hashCode();
        hash = 83 * hash + Objects.hashCode(this.onEvent);
        return hash;
    }

    @Override
    public boolean isCompleted() {
        boolean completed = false;

        switch (onEvent) {
            case (JobEvent.ON_BECAME_DISCARDABLE): {
                completed = job.isDiscardable();
                break;
            }
            case (JobEvent.ON_FAILED): {
                completed = job.isFailed();
                break;
            }
            case (JobEvent.ON_CANCEL): {
                completed = job.isCanceled();
                break;
            }
            case (JobEvent.ON_SUCCEEDED): {
                completed = job.isSuccessfull();
                break;
            }
            case (JobEvent.ON_DONE): {
                completed = job.isDone();
                break;
            }
            default: {
                completed = job.isSuccessfull();
                break;
            }
        }
        return completed;
    }

    @Override
    public Job getJob() {
        return job;
    }
}
