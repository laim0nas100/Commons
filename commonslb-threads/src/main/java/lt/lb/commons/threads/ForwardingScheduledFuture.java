package lt.lb.commons.threads;

import java.util.concurrent.Delayed;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author laim0nas100
 */
public interface ForwardingScheduledFuture<T> extends ForwardingFuture<T>, ScheduledFuture<T> {

    @Override
    public ScheduledFuture<T> delegate();

    @Override
    public default long getDelay(TimeUnit unit) {
        return delegate().getDelay(unit);
    }

    @Override
    public default int compareTo(Delayed o) {
        return delegate().compareTo(o);
    }

}
