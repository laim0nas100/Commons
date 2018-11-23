package lt.lb.commons.misc.rng;

import lt.lb.commons.containers.Value;

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
