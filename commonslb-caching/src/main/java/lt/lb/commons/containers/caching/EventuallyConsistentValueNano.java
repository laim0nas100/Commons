package lt.lb.commons.containers.caching;

import lt.lb.commons.Java;

/**
 *
 * @author laim0nas100
 */
public class EventuallyConsistentValueNano<T> extends EventuallyConsistentValue<T, Long> {

    public EventuallyConsistentValueNano() {
        super(Java::getNanoTime);
    }

    public EventuallyConsistentValueNano(T val) {
        super(val, Java::getNanoTime);
    }
    
}
