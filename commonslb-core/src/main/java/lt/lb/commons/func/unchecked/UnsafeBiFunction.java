package lt.lb.commons.func.unchecked;

import java.util.function.BiFunction;
import lt.lb.commons.misc.NestedException;

/**
 *
 * @author laim0nas100
 */
@FunctionalInterface
public interface UnsafeBiFunction<O, P, R> extends BiFunction<O, P, R> {

    @Override
    public default R apply(O t, P u) {
        try {
            return applyUnsafe(t, u);
        } catch (Throwable e) {
            throw NestedException.of(e);
        }
    }

    public R applyUnsafe(O t, P u) throws Throwable;

}
