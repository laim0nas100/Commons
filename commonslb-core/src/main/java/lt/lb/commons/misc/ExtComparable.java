package lt.lb.commons.misc;

import java.util.Comparator;
import java.util.function.Supplier;

/**
 *
 * @author laim0nas100
 */
public interface ExtComparable<T> extends Comparable<T>, Supplier<T> {

    @Override
    public int compareTo(T o);

    /**
     * @param o1
     * @return o1 &lt me
     */
    public default boolean lessThan(T o1) {
        return this.compareTo(o1) < 0;
    }

    /**
     * @param o1
     * @return o1 &gt me
     */
    public default boolean greaterThan(T o1) {
        return this.compareTo(o1) > 0;
    }

    /**
     * @param o1
     * @return o1 &ge me
     */
    public default boolean greaterThanOrEq(T o1) {
        return this.compareTo(o1) >= 0;
    }

    /**
     * @param o1
     * @return o1 &le me
     */
    public default boolean lessThanOrEq(T o1) {
        return this.compareTo(o1) <= 0;
    }

    /**
     * @param o1
     * @return o1 &ne me
     */
    public default boolean notEqual(T o1) {
        return this.compareTo(o1) != 0;
    }

    /**
     * @param o1
     * @return o1 &eq me
     */
    public default boolean exactly(T o1) {
        return this.compareTo(o1) == 0;
    }

    /**
     *
     * @param o1
     * @return bigger value by this comparator and this value
     */
    public default T max(T o1) {
        return this.greaterThanOrEq(o1) ? o1 : get();
    }

    /**
     *
     * @param o1
     * @return smaller value by this comparator and this value
     */
    public default T min(T o1) {
        return this.lessThanOrEq(o1) ? o1 : get();
    }

    public static <T> ExtComparable<T> fromSupplier(Comparator<T> cmp, Supplier<T> proxy) {
        return new ExtComparable<T>() {
            @Override
            public int compareTo(T o) {
                return cmp.compare(proxy.get(), o);
            }

            @Override
            public T get() {
                return proxy.get();
            }

        };
    }

    public static <T> ExtComparable<T> from(Comparator<T> cmp, T val) {
        return new ExtComparable<T>() {
            @Override
            public int compareTo(T o) {
                return cmp.compare(val, o);
            }

            @Override
            public T get() {
                return val;
            }

        };
    }

}
