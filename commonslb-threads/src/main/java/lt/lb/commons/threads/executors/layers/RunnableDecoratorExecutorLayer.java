package lt.lb.commons.threads.executors.layers;

import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.function.Function;
import lt.lb.commons.threads.executors.ForwardingExecutorService;

/**
 * Anything submitted to this executor gets decorated with provided decorator
 * before submitting to delegating executor.
 *
 * @author laim0nas100
 */
public class RunnableDecoratorExecutorLayer implements ForwardingExecutorService {

    protected ExecutorService exe;
    protected Function<Runnable, Runnable> decorator;

    public RunnableDecoratorExecutorLayer(ExecutorService exe, Function<Runnable, Runnable> decorator) {
        this.exe = Objects.requireNonNull(exe);
        this.decorator = decorator;
    }

    @Override
    public void execute(Runnable command) {
        exe.execute(decorator.apply(command));
    }

    @Override
    public ExecutorService delegate() {
        return exe;
    }
}
