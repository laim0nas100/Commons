package com.github.laim0nas100.commonslb.caching;

import com.github.laim0nas100.commonslb.containers.caching.lazy.LazyProxy;

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

    /**
     * Ensure that the first value is always loaded before second value.
     *
     * @param first
     * @param second
     * @return
     */
    public static Condition ensureLoadOrder(LazyProxy first, LazyProxy second) {
        return now -> first.isLoadedBefore(now) && first.getLoaded() <= second.getLoaded();
    }

}
