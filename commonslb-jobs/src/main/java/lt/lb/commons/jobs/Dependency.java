package lt.lb.commons.jobs;

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
