package lt.lb.commons.threads.executors.layers;

import java.util.concurrent.Executor;
import java.util.function.Function;

/**
 * Anything submitted to this executor gets decorated with provided decorator
 * before submitting to delegating executor.
 *
 * @author laim0nas100
 */
public class RunnableDecoratorExecutorLayer implements Executor {

    protected Executor exe;
    protected Function<Runnable, Runnable> decorator;

    public RunnableDecoratorExecutorLayer(Executor exe, Function<Runnable, Runnable> decorator) {
        this.exe = exe;
        this.decorator = decorator;
    }

    @Override
    public void execute(Runnable command) {
        exe.execute(decorator.apply(command));
    }
}
