package lt.lb.commons;

import java.util.*;
import java.util.function.Predicate;
import java.util.function.Supplier;
import org.apache.commons.lang3.ArrayUtils;

/**
 * Extension of @{link org.apache.commons.lang3.ArrayUtils}
 *
 * @author laim0nas100
 */
public class ArrayOp extends ArrayUtils {

    /**
     * Alternative to using chained &&. Empty/null array results in false.
     *
     * @param array
     * @return
     */
    public static boolean all(Boolean... array) {
        return all(t -> t, array);
    }

    /**
     * Alternative to using chained ||. Empty/null array results in false.
     *
     * @param array
     * @return
     */
    public static boolean any(Boolean... array) {
        return any(t -> t, array);
    }

    /**
     * Checks wether any on the values satisfies given predicate. Empty/null
     * array results in false.
     *
     * @param <T>
     * @param test
     * @param array
     * @return
     */
    public static <T> boolean any(Predicate<T> test, T... array) {

        if (isEmpty(array)) {
            return false;
        }
        for (T t : array) {
            if (test.test(t)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks wether all values satisfies given predicate. Empty/null array
     * results in false.
     *
     * @param <T>
     * @param test
     * @param array
     * @return
     */
    public static <T> boolean all(Predicate<T> test, T... array) {
        return (!isEmpty(array) && !any(test.negate(), array));
    }

    /**
     * Explicit typing for {@link System.arraycopy}
     *
     * @param <T>
     * @param src
     * @param srcPos
     * @param dest
     * @param destPos
     * @param length
     */
    public static <T> void arrayCopy(T[] src, int srcPos, T[] dest, int destPos, int length) {
        System.arraycopy(src, srcPos, dest, destPos, length);
    }

    /**
     * Fully copy source array into a destination array starting at specified
     * destination position. Uses @{link System.arraycopy}.
     *
     * @param <T>
     * @param src
     * @param dest
     * @param destPos
     */
    public static <T> void arrayCopyFullAt(T[] src, T[] dest, int destPos) {
        System.arraycopy(src, 0, dest, destPos, src.length);
    }

    /**
     * Use {@link addAll}
     *
     * @param <T>
     * @param one
     * @param two
     * @return
     * @deprecated
     */
    @Deprecated
    public static <T> T[] merge(T[] one, T... two) {
        return ArrayOp.addAll(one, two);
    }

    /**
     * Merge one or more arrays of same type into a new array
     *
     * @param <T>
     * @param one
     * @param two
     * @return
     */
    public static <T> T[] merge(T[] one, T[]... two) {
        int size = one.length;
        for (T[] t : two) {
            size += t.length;
        }

        Class<T> componentType = (Class<T>) one.getClass().getComponentType();
        T[] newArray = ArrayOp.makeArray(size, componentType);

        int i = one.length;
        arrayCopyFullAt(one, newArray, 0);

        for (T[] t : two) {
            arrayCopyFullAt(t, newArray, i);
            i += t.length;
        }

        return newArray;

    }

    /**
     * Cast array into a whole new array
     *
     * @param <T>
     * @param array
     * @param clz
     * @return
     */
    public static <T> T[] castArray(Object[] array, Class<T> clz) {
        T[] a = makeArray(array.length, clz);
        for (int i = 0; i < array.length; i++) {
            a[i] = (T) array[i];
        }
        return a;
    }

    /**
     * Make new non-primitive type array
     *
     * @param <T>
     * @param size
     * @param clz
     * @return
     */
    public static <T> T[] makeArray(Integer size, Class<T> clz) {
        if (clz.isPrimitive()) {
            throw new IllegalArgumentException("Primitives, like " + clz.getName() + " are not supported, use makePrimitiveArray");
        }
        return (T[]) java.lang.reflect.Array.newInstance(clz, size);
    }

    /**
     * Make new primitive type array (just to separate from non-primitives)
     *
     * @param size
     * @param clz
     * @return
     */
    public static Object makePrimitiveArray(Integer size, Class clz) {
        if (clz.isPrimitive()) {
            return java.lang.reflect.Array.newInstance(clz, size);
        } else {
            throw new IllegalArgumentException(clz.getName() + " is not primitive, use makeArray");
        }
    }

    /**
     * Make new array from list with explicit class.
     *
     * @param <T>
     * @param list
     * @param clz
     * @return
     */
    public static <T> T[] newArray(List<T> list, Class<T> clz) {
        T[] array = makeArray(list.size(), clz);
        return list.toArray(array);
    }

    /**
     * Make new array form iterator
     *
     * @param <T>
     * @param it
     * @param size
     * @param clz
     * @return
     */
    public static <T> T[] newArray(Iterator<T> it, int size, Class<T> clz) {
        T[] array = makeArray(size, clz);

        for (int i = 0; i < size && it.hasNext(); i++) {
            array[i] = it.next();
        }
        return array;
    }

    /**
     *
     * @param <T>
     * @param it
     * @param clz
     * @return
     */
    public static <T> T[] newArrayFill(Iterator<T> it, Class<T> clz) {

        List<T> list = new ArrayList<>();
        it.forEachRemaining(list::add);
        return newArray(list, clz);
    }

    /**
     *
     * @param list
     * @return
     */
    public static Object[] newArray(List list) {
        return list.toArray();
    }

    /**
     * Use {@link removeElements}
     *
     * @param <T>
     * @param one
     * @param two
     * @return
     * @deprecated
     */
    @Deprecated
    public static <T> T[] remove(T[] one, T... two) {
        return ArrayOp.removeElements(one, two);
    }

    /**
     * Use {@link removeAll}
     *
     * @param <T>
     * @param one
     * @param two
     * @return
     */
    public static <T> T[] removeByIndex(T[] one, Integer... two) {
        return ArrayOp.removeAll(one, toPrimitive(two));
    }

    /**
     * Use {@link insert}
     *
     * @param <T>
     * @param one
     * @param where
     * @param what
     * @return
     */
    @Deprecated
    public static <T> T[] addAt(T[] one, Integer where, T... what) {
        return insert(where, one, what);
    }

    /**
     * Removes a strip specified by indices creating a new array
     *
     * @param <T>
     * @param array
     * @param from index start (inclusive)
     * @param to index end (exclusive)
     * @return
     */
    public static <T> T[] removeStrip(T[] array, Integer from, Integer to) {
        if (from > to || from < 0 || to >= array.length) {
            throw new IllegalArgumentException("Arra ranges invalid [" + from + " ," + to + "] and size of:" + array.length);
        }
        int strip = to - from;
        if (strip == 0) {
            return array;
        }
        int size = array.length - strip;
        Class<T> componentType = (Class<T>) array.getClass().getComponentType();
        T[] newArray = ArrayOp.makeArray(size, componentType);

        ArrayOp.arrayCopy(array, 0, newArray, 0, from); // copy first part
        ArrayOp.arrayCopy(array, to, newArray, from, size - from);
        return newArray;
    }

    /**
     * Count how many elements predicate satisfies.
     *
     * @param <T>
     * @param test
     * @param array
     * @return
     */
    public static <T> int count(Predicate<T> test, T... array) {
        int count = 0;
        for (T t : array) {
            count += test.test(t) ? 1 : 0;
        }
        return count;
    }

    /**
     * For variables to array explicitly
     *
     * @param <T>
     * @param vals
     * @return
     */
    public static <T> T[] asArray(T... vals) {
        return vals;
    }

    /**
     * Make copies of given values specified amount of times
     *
     * @param <T>
     * @param times
     * @param values
     * @return
     */
    public static <T> T[] replicate(Integer times, T... values) {
        int arraySize = times * values.length;
        Class<T> cls = (Class<T>) values.getClass().getComponentType();

        if (cls.isPrimitive()) {
            throw new IllegalArgumentException("Primitive values are not supported");
        }
        T[] array = makeArray(arraySize, cls);
        for (int i = 0; i < arraySize; i++) {
            array[i] = values[i % values.length];
        }

        return array;
    }

    /**
     * Make copies of given values specified amount of times using a supplier
     * array
     *
     * @param <T>
     * @param times
     * @param baseClass
     * @param values
     * @return
     */
    public static <T> T[] replicate(Integer times, Class<T> baseClass, Supplier<T>... values) {
        int arraySize = times * values.length;

        if (baseClass.isPrimitive()) {
            throw new IllegalArgumentException("Primitive values are not supported");
        }
        T[] array = makeArray(arraySize, baseClass);
        for (int i = 0; i < arraySize; i++) {
            array[i] = values[i % values.length].get();
        }

        return array;
    }

    /**
     * Explicit method name. Uses {@link toObject}.
     *
     * @param arr
     * @return
     */
    public static Integer[] mapInt(int... arr) {
        return toObject(arr);
    }

    /**
     * Explicit method name. Uses {@link toObject}.
     *
     * @param arr
     * @return
     */
    public static Long[] mapLong(long... arr) {
        return toObject(arr);
    }

    /**
     * Explicit method name. Uses {@link toObject}.
     *
     * @param arr
     * @return
     */
    public static Short[] mapShort(short... arr) {
        return toObject(arr);
    }

    /**
     * Explicit method name. Uses {@link toObject}.
     *
     * @param arr
     * @return
     */
    public static Byte[] mapByte(byte... arr) {
        return toObject(arr);
    }

    /**
     * Explicit method name. Uses {@link toObject}.
     *
     * @param arr
     * @return
     */
    public static Double[] mapDouble(double... arr) {
        return toObject(arr);
    }

    /**
     * Explicit method name. Uses {@link toObject}.
     *
     * @param arr
     * @return
     */
    public static Float[] mapFloat(float... arr) {
        return toObject(arr);
    }

    /**
     * Explicit method name. Uses {@link toObject}.
     *
     * @param arr
     * @return
     */
    public static Boolean[] mapBoolean(boolean... arr) {
        return toObject(arr);
    }

    /**
     * Explicit method name. Uses {@link toObject}.
     *
     * @param arr
     * @return
     */
    public static Character[] mapChar(char... arr) {
        return toObject(arr);
    }

}
