package lt.lb.commons.threads.executors.layers;

import java.util.concurrent.Executor;
import lt.lb.commons.F;

/**
 *
 * If you divide task into blocks, sometimes it happens that a block calls
 * another block, so instead of executing asynchronously, we just extend the
 * same call.
 * 
 * @author laim0nas100
 */
public class NestedTaskSubmitionExecutorLayer implements Executor {

    protected Executor exe;
    protected ThreadLocal<Boolean> inside = ThreadLocal.withInitial(() -> false);

    public NestedTaskSubmitionExecutorLayer(Executor exe) {
        this.exe = exe;
    }

    @Override
    public void execute(Runnable command) {
        if (inside.get()) {
            F.checkedRun(command);
        } else {
            exe.execute(() -> {
                inside.set(true);
                F.checkedRun(command);
                inside.set(false);
            });
        }
    }

}
