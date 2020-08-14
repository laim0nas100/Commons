package lt.lb.commons.containers.caching;

import java.util.function.Supplier;

/**
 *
 * boolean primitive condition check.
 *
 * @author laim0nas100
 */
@FunctionalInterface
public interface Condition extends Supplier<Boolean> {

    public boolean isTrue();

    @Override
    public default Boolean get() {
        return isTrue();
    }

    public default boolean isFalse() {
        return !isTrue();
    }
}
