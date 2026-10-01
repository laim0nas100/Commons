package com.github.laim0nas100.commonslb.threads.executors.scheduled;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicReference;
import com.github.laim0nas100.commonslb.threads.ForwardingScheduledFuture;

/**
 * Forwarding scheduled future with persistent cancel state
 *
 * @author laim0nas100
 */
public class PersistentForwardingScheduledFuture<T> extends PersistentCancel<T, ScheduledFuture<T>> implements ForwardingScheduledFuture<T> {


    public PersistentForwardingScheduledFuture() {
        super(new AtomicReference<>());
    }

    @Override
    public ScheduledFuture<T> delegate() {
        return getRef();
    }

}
