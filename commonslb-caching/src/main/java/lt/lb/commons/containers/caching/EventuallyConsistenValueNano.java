package lt.lb.commons.containers.caching;

import lt.lb.commons.Java;

/**
 *
 * @author laim0nas100
 */
public class EventuallyConsistenValueNano<T> extends EventuallyConsistentValue<T, Long> {

    public EventuallyConsistenValueNano() {
        super(Java::getNanoTime);
    }

    public EventuallyConsistenValueNano(T val) {
        super(val, Java::getNanoTime);
    }
    
}
