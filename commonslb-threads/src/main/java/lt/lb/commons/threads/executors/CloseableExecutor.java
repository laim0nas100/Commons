package lt.lb.commons.threads.executors;

import java.util.concurrent.Executor;

/**
 *
 * @author laim0nas100
 */
public interface CloseableExecutor extends Executor, AutoCloseable {

    public default void shutdown() {
        close();
    }
    
    @Override
    public void close();
}
