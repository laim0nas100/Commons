package lt.lb.commons.jobs;

import java.util.Objects;
import java.util.Optional;

/**
 *
 * @author laim0nas100
 * @param <T>
 */
public class JobEvent<T> {

    private final String eventName;
    private final Job createdBy;
    private Optional<T> data;

    public String getEventName() {
        return eventName;
    }

    public Job getCreator() {
        return this.createdBy;
    }

    public Optional<T> getData() {
        return data;
    }

    public JobEvent(String eventName, Job source) {
        this(eventName, source, null);
    }

    public JobEvent(String eventName, Job source, T data) {
        this.eventName = Objects.requireNonNull(eventName);
        this.createdBy = Objects.requireNonNull(source);
        this.data = Optional.ofNullable(data);
    }
}
