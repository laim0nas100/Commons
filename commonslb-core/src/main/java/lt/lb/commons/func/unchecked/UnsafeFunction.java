package lt.lb.commons.func.unchecked;

import java.util.function.Function;
import lt.lb.commons.misc.NestedException;

/**
 *
 * @author laim0nas100
 */
@FunctionalInterface
public interface UnsafeFunction<P, R> extends Function<P, R> {

    public R applyUnsafe(P t) throws Throwable;

    @Override
    public default R apply(P t) {
        try {
            return applyUnsafe(t);
        } catch (Throwable e) {
            throw NestedException.of(e);
        }
    }
}
