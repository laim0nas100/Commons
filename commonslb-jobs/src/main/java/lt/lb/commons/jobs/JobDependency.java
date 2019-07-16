package lt.lb.commons.jobs;

/**
 *
 * used to check if job state has been met to schedule a job, that has this dependency
 * @author laim0nas100
 */
public interface JobDependency<T> {

    @Override
    public boolean equals(Object o);

    @Override
    public int hashCode();

    /**
     * Wether dependency is satisfied
     * @return 
     */
    public boolean isCompleted();

    /**
     * Job that comes with dependency
     * @return 
     */
    public Job<T> getJob();
}
