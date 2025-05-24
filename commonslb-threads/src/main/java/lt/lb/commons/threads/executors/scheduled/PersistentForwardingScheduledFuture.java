package lt.lb.commons.threads.executors.scheduled;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicReference;
import lt.lb.commons.threads.ForwardingScheduledFuture;

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
