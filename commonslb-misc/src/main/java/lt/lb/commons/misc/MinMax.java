package lt.lb.commons.misc;

import java.util.Objects;

/**
 * Simple 2 value holder class.
 *
 * @author laim0nas100
 * @param <T>
 */
public class MinMax<T> {

    public final T min, max;

    public MinMax(T min, T max) {
        this.min = Objects.requireNonNull(min);
        this.max = Objects.requireNonNull(max);
    }

    @Override
    public String toString() {
        return min + " " + max;
    }
}
