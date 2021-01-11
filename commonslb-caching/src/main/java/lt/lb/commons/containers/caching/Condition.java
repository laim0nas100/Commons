package lt.lb.commons.containers.caching;


/**
 *
 * boolean primitive condition check.
 *
 * @author laim0nas100
 */
@FunctionalInterface
public interface Condition {

    public boolean isTrue(long now);

    public default boolean isFalse(long now) {
        return !isTrue(now);
    }

}
