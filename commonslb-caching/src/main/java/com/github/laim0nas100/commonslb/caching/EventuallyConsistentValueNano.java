package com.github.laim0nas100.commonslb.caching;

import com.github.laim0nas100.commonslb.Java;

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
