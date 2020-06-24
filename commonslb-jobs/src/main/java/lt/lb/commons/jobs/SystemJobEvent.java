package lt.lb.commons.jobs;

/**
 *
 * @author laim0nas100
 */
public class SystemJobEvent extends JobEvent{

    public final SystemJobEventName enumName;
    
    public SystemJobEvent(SystemJobEventName event, Job source) {
        super(event.eventName, source);
        enumName = event;
    }

    public SystemJobEvent(SystemJobEventName event, Job source, Object data) {
        super(event.eventName, source, data);
        enumName = event;
    }
    
}
