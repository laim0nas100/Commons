package lt.lb.commons.func.unchecked;

import java.util.Optional;
import java.util.concurrent.Callable;
import lt.lb.commons.F;
import lt.lb.commons.misc.NestedException;

/**
 *
 * @author laim0nas100
 */
@FunctionalInterface
public interface UncheckedRunnable extends Runnable {
    
    public static UncheckedRunnable from(Callable call) {
        return () -> call.call();
    }
    
    public static UncheckedRunnable from(Runnable run) {
        return () -> run.run();
    }
    
    @Override
    public default void run() {
        
        try {
            this.uncheckedRun();
        } catch (Throwable e) {
            throw NestedException.of(e);
        }
    }
    
    public void uncheckedRun() throws Throwable;
    
    public static Callable toCallable(UncheckedRunnable run) {
        return () -> {
            Optional<Throwable> checkedRun = F.checkedRun(run).map(m -> NestedException.unwrap(m));
            if (checkedRun.isPresent()) {
                Throwable get = checkedRun.get();
                if (get instanceof Exception) {
                    throw (Exception) get;
                } else {
                    throw NestedException.of(get);
                }
            }
            return null;
        };
    }
}
