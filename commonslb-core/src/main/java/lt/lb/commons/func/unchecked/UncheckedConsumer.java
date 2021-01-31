package lt.lb.commons.func.unchecked;

import java.util.function.Consumer;
import lt.lb.commons.misc.NestedException;

/**
 *
 * @author laim0nas100
 * @param <P>
 */
@FunctionalInterface
public interface UncheckedConsumer<P> extends Consumer<P> {


    public void applyUnchecked(P t) throws Throwable;

    @Override
    public default void accept(P t) {
        try {
            applyUnchecked(t);
        } catch (Throwable e) {
            throw NestedException.of(e);
        }
    }
    
    
}
