/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package LibraryLB.Jobs;

/**
 *
 * @author Lemmin
 */
public class JobEvent {

    public static final String ON_DONE = "onDone";
    public static final String ON_CANCEL = "onCancel";
    public static final String ON_FAILED = "onFailed";
    public static final String ON_FINISHED = "onFinished";
    public static final String ON_SUCCEEDED = "onSucceeded";

    private String eventName;
    private Job createdBy;
    private Object data;

    public String getEventName() {
        return eventName;
    }

    public Job getCreator() {
        return this.createdBy;
    }

    public Object getData() {
        return data;
    }

    public JobEvent(String eventName, Job source) {
        this.eventName = eventName;
        this.createdBy = source;
    }

    public JobEvent(String eventName, Job source, Object data) {
        this(eventName, source);
        this.data = data;
    }
}
