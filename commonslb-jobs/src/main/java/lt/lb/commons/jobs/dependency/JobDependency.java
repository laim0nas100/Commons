package lt.lb.commons.jobs.dependency;

import lt.lb.commons.jobs.Job;

/**
 *
 * Dependency with explicit job association. 
 * @author laim0nas100
 */
public interface JobDependency<T> extends Dependency {

    

    /**
     * Job that comes with dependency
     * @return 
     */
    public Job<T> getJob();
}
