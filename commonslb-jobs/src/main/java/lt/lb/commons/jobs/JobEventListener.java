package lt.lb.commons.jobs;

/**
 *
 * @author laim0nas100
 * @param <T>
 */
public interface JobEventListener<T> {

    public void onEvent(JobEvent<T> event);
}
