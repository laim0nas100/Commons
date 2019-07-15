package lt.lb.commons.jobs;

/**
 *
 * @author laim0nas100
 */
public interface JobDependency<T> {

    @Override
    public boolean equals(Object o);

    @Override
    public int hashCode();

    public boolean isCompleted();

    public Job<T> getJob();
}
