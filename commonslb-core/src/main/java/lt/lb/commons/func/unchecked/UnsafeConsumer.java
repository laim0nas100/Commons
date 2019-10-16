package lt.lb.commons.func.unchecked;

import java.util.function.Consumer;
import lt.lb.commons.misc.NestedException;

/**
 *
 * @author laim0nas100
 * @param <P>
 */
@FunctionalInterface
public interface UnsafeConsumer<P> extends Consumer<P> {


    public void applyUnsafe(P t) throws Throwable;

    public default void accept(P t) {
        try {
            applyUnsafe(t);
        } catch (Throwable e) {
            throw NestedException.of(e);
        }
    }
    
    
}
