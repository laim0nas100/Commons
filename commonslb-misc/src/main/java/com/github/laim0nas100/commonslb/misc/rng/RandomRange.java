package com.github.laim0nas100.commonslb.misc.rng;

import com.github.laim0nas100.commonslb.containers.values.Value;

/**
 *
 * @author laim0nas100
 */
public class RandomRange<T> extends Value<T> {

    public final Double span;
    public boolean disabled = false;

    public RandomRange(T value, Double span) {
        this.value = value;
        this.span = span;
    }

}
