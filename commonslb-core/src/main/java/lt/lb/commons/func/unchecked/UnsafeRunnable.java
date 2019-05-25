package lt.lb.commons.func.unchecked;

import java.util.concurrent.Callable;
import lt.lb.commons.misc.NestedException;

/**
 *
 * @author laim0nas100
 */
@FunctionalInterface
public interface UnsafeRunnable extends Runnable {

    public static UnsafeRunnable from(Callable call) {
        return () -> call.call();
    }

    public static UnsafeRunnable from(Runnable run) {
        return () -> run.run();
    }

    @Override
    public default void run() {

        try {
            this.unsafeRun();
        } catch (Throwable e) {
            throw NestedException.of(e);
        }
    }

    public void unsafeRun() throws Throwable;
}
