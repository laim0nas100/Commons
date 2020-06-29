package lt.lb.commons.jobs.dependency;

/**
 * Dependency with no job association. 
 * @author laim0nas100
 */
public interface Dependency {


    /**
     * Wether dependency is satisfied
     *
     * @return
     */
    public boolean isCompleted();
}
