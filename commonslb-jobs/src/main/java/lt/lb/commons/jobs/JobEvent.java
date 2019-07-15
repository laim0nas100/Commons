package lt.lb.commons.jobs;

import java.util.Optional;

/**
 *
 * @author laim0nas100
 */
public class JobEvent {

    public static final String ON_DONE = "onDone";
    public static final String ON_CANCEL = "onCancel";
    public static final String ON_FAILED = "onFailed";
    public static final String ON_BECAME_DISCARDABLE = "onFinished";
    public static final String ON_SUCCEEDED = "onSucceeded";
    public static final String ON_SCHEDULED = "onScheduled";
    public static final String ON_FAILED_TO_START = "onFailedToStart";
    public static final String ON_EXECUTE = "onExecute";

    private String eventName;
    private Job createdBy;
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
        this.eventName = eventName;
        this.createdBy = source;
        this.data = Optional.ofNullable(data);
    }
}
