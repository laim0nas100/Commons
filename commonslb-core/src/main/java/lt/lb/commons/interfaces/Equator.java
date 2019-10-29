package lt.lb.commons.interfaces;

import java.util.Objects;
import java.util.function.Function;

/**
 *
 * @author laim0nas100
 * @param <T> type
 */
public interface Equator<T> {

    /**
     * Class to redefine objects "equals" and "hashCode" methods, to change what it means to be equal
     * @param <T> 
     */
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
            return eq.hashCode(value);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if(obj instanceof EqualityProxy){
                final EqualityProxy<T> other = (EqualityProxy<T>) obj;
                return eq.equals(value, other.value);
            }else{
                return false;
            }
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
    
    /**
     * Custom hash code (0 by default to all values)
     * @param value
     * @return 
     */
    public default int hashCode(T value){
        return 0;
    }


    /**
     * Value and hashing property are the same
     *
     * @param <T>
     * @return
     */
    public static <T> Equator<T> primitiveHashEquator() {
        return new Equator<T>() {
            @Override
            public int hashCode(T val) {
                return Objects.hashCode(val);
            }

            @Override
            public boolean equals(T value1, T value2) {
                return Objects.equals(value1, value2);
            }
        };
    }

    /**
     * Use the Objects.equal with no hashing
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
    public static <T, V> Equator<T> valueHashEquator(Function<T, V> resolver) {
        return new Equator<T>() {
            public boolean equals(T value1, T value2) {
                return Objects.equals(resolver.apply(value1), resolver.apply(value2));
            }

            public int hashCode(T val) {
                return Objects.hashCode(resolver.apply(val));
            }
        };
    }
}
