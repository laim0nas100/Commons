package lt.lb.commons.func.unchecked;

import java.util.function.Function;
import lt.lb.commons.misc.NestedException;

/**
 *
 * @author laim0nas100
 */
@FunctionalInterface
public interface UncheckedFunction<P, R> extends Function<P, R> {

    public R applyUnchecked(P t) throws Throwable;

    @Override
    public default R apply(P t) {
        try {
            return applyUnchecked(t);
        } catch (Throwable e) {
            throw NestedException.of(e);
        }
    }
}
