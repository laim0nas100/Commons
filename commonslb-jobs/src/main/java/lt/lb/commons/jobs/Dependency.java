package lt.lb.commons.jobs;

/**
 * Dependency with no job association. 
 * @author laim0nas100
 */
public interface Dependency {

    @Override
    public boolean equals(Object o);

    @Override
    public int hashCode();

    /**
     * Wether dependency is satisfied
     *
     * @return
     */
    public boolean isCompleted();
}
