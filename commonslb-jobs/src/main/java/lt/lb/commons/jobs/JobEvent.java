package lt.lb.commons.jobs;

import java.util.Objects;
import java.util.Optional;

/**
 *
 * @author laim0nas100
 */
public class JobEvent {

    private final String eventName;
    private final Job createdBy;
    private Optional<Object> data;

    public String getEventName() {
        return eventName;
    }

    public Job getCreator() {
        return this.createdBy;
    }

    public Optional<Object> getData() {
        return data;
    }

    public JobEvent(String eventName, Job source) {
        this(eventName, source, null);
    }

    public JobEvent(String eventName, Job source, Object data) {
        this.eventName = Objects.requireNonNull(eventName);
        this.createdBy = Objects.requireNonNull(source);
        this.data = Optional.ofNullable(data);
    }
}
