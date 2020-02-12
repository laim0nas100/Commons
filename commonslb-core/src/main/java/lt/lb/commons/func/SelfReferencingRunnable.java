package lt.lb.commons.func;

import java.util.function.Consumer;

/**
 *
 * @author laim0nas100
 */
public interface SelfReferencingRunnable extends Runnable, Consumer<Runnable> {

    @Override
    public default void run() {
        this.accept(this);
    }

}
