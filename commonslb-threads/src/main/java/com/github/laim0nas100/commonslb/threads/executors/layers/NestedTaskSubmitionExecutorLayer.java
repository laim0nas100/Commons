package com.github.laim0nas100.commonslb.threads.executors.layers;

import java.util.Objects;
import java.util.concurrent.ExecutorService;
import com.github.laim0nas100.commonslb.threads.executors.ForwardingExecutorService;
import com.github.laim0nas100.uncheckedutils.Checked;

/**
 *
 * If you divide task into blocks, sometimes it happens that a block calls
 * another block, so instead of executing asynchronously, we just extend the
 * same call.
 *
 * @author laim0nas100
 */
public class NestedTaskSubmitionExecutorLayer implements ForwardingExecutorService {

    protected ExecutorService exe;
    protected ThreadLocal<Boolean> inside = ThreadLocal.withInitial(() -> false);

    public NestedTaskSubmitionExecutorLayer(ExecutorService exe) {
        this.exe = Objects.requireNonNull(exe);
    }

    @Override
    public void execute(Runnable command) {
        if (inside.get()) {
            Checked.checkedRun(command);
        } else {
            exe.execute(() -> {
                inside.set(true);
                Checked.checkedRun(command);
                inside.set(false);
            });
        }
    }

    public boolean inside() {
        return inside.get();
    }

    @Override
    public ExecutorService delegate() {
        return exe;
    }

}
