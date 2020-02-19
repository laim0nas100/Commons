package lt.lb.commons.func;

import java.util.function.Consumer;

/**
 *
 * Self referencing runnable. If repeatedly called in the same thread results in
 * StackOverflowExcetion, so don't use to chain actions, instead use to define
 * same calls in other events, witch continues in a new thread or from new
 * stack. Convenient for organizing events like UI rebuilding which calls the
 * same UI rebuild at some event or condition which is defined at current UI
 * rebuild.
 *
 * @author laim0nas100
 */
public interface SelfReferencingRunnable extends Runnable, Consumer<Runnable> {

    @Override
    public default void run() {
        this.accept(this);
    }

}
