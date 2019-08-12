package lt.lb.commons.jobs;

import java.util.Optional;

/**
 *
 * @author laim0nas100
 */
public class JobEvent {

    /**
     * When Job becomes done. (failed || succeeded || cancelled)
     */
    public static final String ON_DONE = "onDone";
    /**
     * When Job becomes cancelled.
     */
    public static final String ON_CANCEL = "onCancel";
    /**
     * When Job becomes failed.
     */
    public static final String ON_FAILED = "onFailed";
    /**
     * When Job becomes discarded.
     */
    public static final String ON_DISCARDED = "onDiscarded";
    
    /**
     * When Job becomes succeeded.
     */
    public static final String ON_SUCCEEDED = "onSucceeded";
    /**
     * When Job becomes scheduled.
     */
    public static final String ON_SCHEDULED = "onScheduled";
    /**
     * When Job fails to start after being scheduled and then de-scheduled.
     */
    public static final String ON_FAILED_TO_START = "onFailedToStart";
    /**
     * When Job fails starts after being scheduled.
     */
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
