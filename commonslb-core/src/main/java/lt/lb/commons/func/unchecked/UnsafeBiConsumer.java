package lt.lb.commons.func.unchecked;

import java.util.function.BiConsumer;
import lt.lb.commons.misc.NestedException;

/**
 *
 * @author laim0nas100
 * @param <P>
 * @param <R>
 */
@FunctionalInterface
public interface UnsafeBiConsumer<P, R> extends BiConsumer<P, R> {

    public void applyUnsafe(P t, R r) throws Throwable;

    public default void accept(P t, R r) {
        try {
            applyUnsafe(t, r);
        } catch (Throwable e) {
            throw NestedException.of(e);
        }
    }

}
