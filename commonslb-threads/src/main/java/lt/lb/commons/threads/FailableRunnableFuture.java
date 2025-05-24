package lt.lb.commons.threads;

import java.util.concurrent.RunnableFuture;

/**
 *
 * @author laim0nas100
 */
public interface FailableRunnableFuture<T> extends RunnableFuture<T> {

    public void setException(Throwable t);
}
