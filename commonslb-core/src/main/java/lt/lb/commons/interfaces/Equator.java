package lt.lb.commons.interfaces;

import java.util.Objects;
import java.util.function.Function;
import lt.lb.commons.ArrayOp;

/**
 * redefine objects "equals" and "hashCode" methods, to change what it means to
 * be equal. Primarily to use with hash maps or sets, to find intersections by
 * defined property
 *
 * @author laim0nas100
 * @param <T> type
 */
public interface Equator<T> {

    /**
     * Class to redefine objects "equals" and "hashCode" methods, to change what
     * it means to be equal
     *
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
            if (obj instanceof EqualityProxy) {
                final EqualityProxy<T> other = (EqualityProxy<T>) obj;
                return eq.equals(value, other.value);
            } else {
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
     *
     * @param value
     * @return
     */
    public default int hashCode(T value) {
        return 0;
    }

    public static int deepHashCode(Equator eq, Object a[]) {
        if (a == null) {
            return 0;
        }

        int result = 1;

        for (Object element : a) {
            final int elementHash;
            final Class<?> cl;
            if (element == null) {
                elementHash = 0;
            } else if ((cl = element.getClass().getComponentType()) == null) {
                elementHash = eq.hashCode(element);
            } else if (element instanceof Object[]) {
                elementHash = deepHashCode(eq, (Object[]) element);
            } else {
                elementHash = primitiveArrayHashCode(eq, element, cl);
            }

            result = 31 * result + elementHash;
        }

        return result;
    }

    public static int primitiveArrayHashCode(Equator eq, Object a, Class<?> cl) {
        if ((cl == byte.class)) {
            return deepHashCode(eq, ArrayOp.mapByte((byte[]) a));
        } else if ((cl == int.class)) {
            return deepHashCode(eq, ArrayOp.mapInt((int[]) a));
        } else if (cl == long.class) {
            return deepHashCode(eq, ArrayOp.mapLong((long[]) a));
        } else if (cl == char.class) {
            return deepHashCode(eq, ArrayOp.mapChar((char[]) a));
        } else if (cl == short.class) {
            return deepHashCode(eq, ArrayOp.mapShort((short[]) a));
        } else if (cl == boolean.class) {
            return deepHashCode(eq, ArrayOp.mapBoolean((boolean[]) a));
        } else if (cl == double.class) {
            return deepHashCode(eq, ArrayOp.mapDouble((double[]) a));
        } else if (cl == float.class) {
            return deepHashCode(eq, ArrayOp.mapFloat((float[]) a));
        } else {
            throw new IllegalArgumentException("Unrecognized primitive class " + cl);
        }
    }

    public static boolean deepEqualsArray(Equator equator, Object[] a1, Object[] a2) {
        if (a1 == a2) {
            return true;
        }
        if (a1 == null || a2 == null) {
            return false;
        }
        int length = a1.length;
        if (a2.length != length) {
            return false;
        }

        for (int i = 0; i < length; i++) {
            // Figure out whether the two elements are equal
            if(!deepEquals(equator, a1[i], a2[i])){
                return false;
            }
        }
        return true;
    }

    public static boolean deepEquals(Equator equator, Object e1, Object e2) {
        boolean eq;
        if (e1 instanceof Object[] && e2 instanceof Object[]) {
            eq = deepEqualsArray(equator, (Object[]) e1, (Object[]) e2);
        } else if (e1 instanceof byte[] && e2 instanceof byte[]) {
            eq = deepEqualsArray(equator, ArrayOp.mapByte((byte[]) e1), ArrayOp.mapByte((byte[]) e2));
        } else if (e1 instanceof short[] && e2 instanceof short[]) {
            eq = deepEqualsArray(equator, ArrayOp.mapShort((short[]) e1), ArrayOp.mapShort((short[]) e2));
        } else if (e1 instanceof int[] && e2 instanceof int[]) {
            eq = deepEqualsArray(equator, ArrayOp.mapInt((int[]) e1), ArrayOp.mapInt((int[]) e2));
        } else if (e1 instanceof long[] && e2 instanceof long[]) {
            eq = deepEqualsArray(equator, ArrayOp.mapLong((long[]) e1), ArrayOp.mapLong((long[]) e2));
        } else if (e1 instanceof char[] && e2 instanceof char[]) {
            eq = deepEqualsArray(equator, ArrayOp.mapChar((char[]) e1), ArrayOp.mapChar((char[]) e2));
        } else if (e1 instanceof float[] && e2 instanceof float[]) {
            eq = deepEqualsArray(equator, ArrayOp.mapFloat((float[]) e1), ArrayOp.mapFloat((float[]) e2));
        } else if (e1 instanceof double[] && e2 instanceof double[]) {
            eq = deepEqualsArray(equator, ArrayOp.mapDouble((double[]) e1), ArrayOp.mapDouble((double[]) e2));
        } else if (e1 instanceof boolean[] && e2 instanceof boolean[]) {
            eq = deepEqualsArray(equator, ArrayOp.mapBoolean((boolean[]) e1), ArrayOp.mapBoolean((boolean[]) e2));
        } else {
            eq = equator.equals(e1, e2);
        }
        return eq;
    }

    /**
     * Use the Objects.equal with no hashing
     *
     * @param <T>
     */
    public static interface PrimitiveEquator<T> extends Equator<T> {

        @Override
        public default boolean equals(T value1, T value2) {
            return Objects.equals(value1, value2);
        }

    }

    /**
     * Value and hashing property are the same
     *
     * @param <T>
     */
    public static interface PrimitiveHashEquator<T> extends Equator<T> {

        @Override
        public default int hashCode(T val) {
            return Objects.hashCode(val);
        }

        @Override
        public default boolean equals(T value1, T value2) {
            return Objects.equals(value1, value2);
        }
    }

    /**
     * Only use the value provided by resolver
     *
     * @param <T>
     * @param <V>
     */
    public static interface ValueEquator<T, V> extends Equator<T>, Function<T, V> {

        @Override
        public default boolean equals(T value1, T value2) {
            return Objects.equals(apply(value1), apply(value2));
        }
    }

    /**
     * Only use the value provided by resolver, hash by the same value
     *
     * @param <T>
     * @param <V>
     */
    public static interface ValueHashEquator<T, V> extends ValueEquator<T, V> {

        @Override
        public default int hashCode(T value) {
            return Objects.hashCode(apply(value));
        }

    }

    /**
     * Value and hashing property are the same
     *
     * @param <T>
     * @return
     */
    public static <T> Equator<T> primitiveHashEquator() {
        return new PrimitiveHashEquator<T>() {
        };
    }

    /**
     * Use the Objects.equal with no hashing
     *
     * @param <T>
     * @return
     */
    public static <T> Equator<T> primitiveEquator() {
        return new PrimitiveEquator<T>() {
        };
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
        return (ValueEquator<T, V>) resolver::apply;
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
        return (ValueHashEquator<T, V>) resolver::apply;
    }
}
