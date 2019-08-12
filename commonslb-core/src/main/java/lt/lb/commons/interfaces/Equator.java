package lt.lb.commons.interfaces;

import java.util.Objects;
import java.util.function.Function;
import lt.lb.commons.F;
import lt.lb.commons.containers.values.BooleanValue;

/**
 *
 * @author laim0nas100
 * @param <T> type
 */
public interface Equator<T> {

    public static class EqualityHashProxy<T> extends EqualityProxy<T> {

        public EqualityHashProxy(T value, HashEquator<T> eq) {
            super(value, eq);
        }

        @Override
        public int hashCode() {
            HashEquator e = F.cast(eq);
            return Objects.hashCode(e.getHashable(value));
        }

    }

    public static class EqualityProxy<T> {

        protected final T value;
        protected final Equator<T> eq;

        public EqualityProxy(T value, Equator<T> eq) {
            this.value = value;
            this.eq = eq;
        }

        public T getValue() {
            return value;
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(value);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            final EqualityProxy<?> other = (EqualityProxy<?>) obj;
            return eq.genericEquals(this.value, other.value);
        }

    }

    /**
     * Custom equals
     *
     * @param value1
     * @param value2
     * @return
     */
    public boolean equals(T value1, T value2);

    public default boolean genericEquals(Object v1, Object v2) {
        if (v1 == v2) {
            return true;
        }
        if ((v1 == null) != (v2 == null)) {
            return false;
        }
        BooleanValue b = BooleanValue.FALSE();
        F.checkedRun(() -> {
            b.set(equals(F.cast(v1), F.cast(v2)));
        });
        return b.get();
    }

    public interface HashEquator<T> extends Equator<T> {

        /**
         *
         * @param val value in question
         * @return same value or property (i.e. ID) to store in hash for quick
         * look-up
         */
        public Object getHashable(T val);
    }

    /**
     * Value and hashing property are the same
     *
     * @param <T>
     * @return
     */
    public static <T> HashEquator<T> primitiveHashEquator() {
        return new HashEquator<T>() {
            @Override
            public Object getHashable(T val) {
                return val;
            }

            @Override
            public boolean equals(T value1, T value2) {
                return Objects.equals(value1, value2);
            }
        };
    }

    /**
     * Use the Objects.equal
     *
     * @param <T>
     * @return
     */
    public static <T> Equator<T> primitiveEquator() {
        return (a, b) -> Objects.equals(a, b);
    }

    /**
     * Only use the value provided by resolver
     *
     * @param <T>
     * @param <V>
     * @param resolver
     * @return
     */
    public static <T, V> Equator<T> valueEquator(Function<T, V> resolver) {
        return (T value1, T value2) -> Objects.equals(resolver.apply(value1), resolver.apply(value2));
    }

    /**
     * Only use the value provided by resolver, hash by the same value
     *
     * @param <T>
     * @param <V>
     * @param resolver
     * @return
     */
    public static <T, V> HashEquator<T> valueHashEquator(Function<T, V> resolver) {
        return new HashEquator<T>() {
            @Override
            public boolean equals(T value1, T value2) {
                return Objects.equals(resolver.apply(value1), resolver.apply(value2));
            }

            @Override
            public Object getHashable(T val) {
                return resolver.apply(val);
            }
        };
    }
}
