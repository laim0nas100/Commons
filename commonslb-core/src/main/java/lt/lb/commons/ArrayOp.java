package lt.lb.commons;

import java.util.*;
import java.util.function.Predicate;
import java.util.function.Supplier;
import org.apache.commons.lang3.ArrayUtils;

/**
 *
 * @author laim0nas100
 */
public class ArrayOp extends ArrayUtils{

    public static <T> boolean any(Predicate<T> test, T... array) {
        if (array.length == 0) {
            return false;
        }
        for (T t : array) {
            if (test.test(t)) {
                return true;
            }
        }
        return false;
    }

    public static <T> boolean all(Predicate<T> test, T... array) {
        return (array.length > 0 && !any(test.negate(), array));
    }

    public static <T> void arrayCopy(T[] src, int srcPos, T[] dest, int destPos, int length) {
        System.arraycopy(src, srcPos, dest, destPos, length);
    }

    public static <T> void arrayCopyFullAt(T[] src, T[] dest, int destPos) {
        System.arraycopy(src, 0, dest, destPos, src.length);
    }

    public static <T> T[] merge(T[] one, T... two) {
        int size = one.length + two.length;
        Class<T> componentType = (Class<T>) one.getClass().getComponentType();
        T[] newArray = ArrayOp.makeArray(size, componentType);

        arrayCopyFullAt(one, newArray, 0);
        arrayCopyFullAt(two, newArray, one.length);
        return newArray;

    }

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

    public static <T> T[] castArray(Object[] array, Class<T> clz) {
        T[] a = makeArray(array.length, clz);
        for (int i = 0; i < array.length; i++) {
            a[i] = (T) array[i];
        }
        return a;
    }

    public static <T> T[] makeArray(Integer size, Class<T> clz) {
        if (clz.isPrimitive()) {
            throw new IllegalArgumentException("Primitives, like " + clz.getName() + " are not supported, use makePrimitiveArray");
        }
        return (T[]) java.lang.reflect.Array.newInstance(clz, size);
    }

    public static Object makePrimitiveArray(Integer size, Class clz) {
        if (clz.isPrimitive()) {
            return java.lang.reflect.Array.newInstance(clz, size);
        } else {
            throw new IllegalArgumentException(clz.getName() + " is not primitive, use makeArray");
        }
    }

    public static <T> T[] newArray(List<T> list, Class<T> clz) {
        T[] array = makeArray(list.size(), clz);
        return list.toArray(array);
    }

    public static <T> T[] newArray(Iterator<T> it, int size, Class<T> clz) {
        T[] array = makeArray(size, clz);
        F.iterate(it, (i, item) -> {
            array[i] = item;
        });
        return array;
    }

    public static <T> T[] newArrayFill(Iterator<T> it, Class<T> clz) {
        LinkedList<T> list = new LinkedList<>();
        it.forEachRemaining(list::add);
        return newArray(list, clz);
    }

    public static Object[] newArray(List list) {
        return newArray(list, Object.class);
    }

    public static <T> T[] remove(T[] one, T... two) {
        ArrayList<T> list = new ArrayList<>(one.length);
        HashSet<T> set = new HashSet<>();

        for (T t : two) {
            set.add(t);
        }
        for (T t : one) {
            if (!set.contains(t)) {
                list.add(t);
            }
        }

        Class<T> componentType = (Class<T>) one.getClass().getComponentType();
        return newArray(list, componentType);
    }

    public static <T> T[] removeByIndex(T[] one, Integer... two) {
        ArrayList<T> list = new ArrayList<>(one.length);
        HashSet<Integer> set = new HashSet<>();
        set.addAll(Arrays.asList(two));
        int i = 0;
        for (T t : one) {
            if (!set.contains(i)) {
                list.add(t);
            }
            i++;
        }

        Class<T> componentType = (Class<T>) one.getClass().getComponentType();
        return newArray(list, componentType);
    }

    public static <T> T[] addAt(T[] one, Integer where, T... what) {
        int size = one.length + what.length;
        Class<T> componentType = (Class<T>) one.getClass().getComponentType();
        T[] newArray = ArrayOp.makeArray(size, componentType);

        ArrayOp.arrayCopy(one, 0, newArray, 0, where); // copy first part
        ArrayOp.arrayCopyFullAt(what, newArray, where);
        ArrayOp.arrayCopy(one, where, newArray, where + what.length, one.length - where);
        return newArray;

    }

    public static <T> T[] removeStrip(T[] one, Integer from, Integer to) {

        int strip = to - from;
        int size = one.length - strip;
        Class<T> componentType = (Class<T>) one.getClass().getComponentType();
        T[] newArray = ArrayOp.makeArray(size, componentType);

        ArrayOp.arrayCopy(one, 0, newArray, 0, from); // copy first part
        ArrayOp.arrayCopy(one, to, newArray, from, size - from);
        return newArray;
    }

    public static <T> int count(Predicate<T> test, T... array) {
        int count = 0;
        for (T t : array) {
            count += test.test(t) ? 1 : 0;
        }
        return count;
    }

    public static <T> T[] asArray(T... vals) {
        return vals;
    }

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

    public static Integer[] mapInt(int... arr) {
        Integer[] a = new Integer[arr.length];
        for (int i = 0; i < arr.length; i++) {
            a[i] = arr[i];
        }
        return a;
    }

    public static Long[] mapLong(long... arr) {
        Long[] a = new Long[arr.length];
        for (int i = 0; i < arr.length; i++) {
            a[i] = arr[i];
        }
        return a;
    }

    public static Short[] mapsShort(short... arr) {
        Short[] a = new Short[arr.length];
        for (int i = 0; i < arr.length; i++) {
            a[i] = arr[i];
        }
        return a;
    }

    public static Byte[] mapByte(byte... arr) {
        Byte[] a = new Byte[arr.length];
        for (int i = 0; i < arr.length; i++) {
            a[i] = arr[i];
        }
        return a;
    }

    public static Double[] mapDouble(double... arr) {
        Double[] a = new Double[arr.length];
        for (int i = 0; i < arr.length; i++) {
            a[i] = arr[i];
        }
        return a;
    }

    public static Float[] mapFloat(float... arr) {
        Float[] a = new Float[arr.length];
        for (int i = 0; i < arr.length; i++) {
            a[i] = arr[i];
        }
        return a;
    }

    public static Boolean[] mapBoolean(boolean... arr) {
        Boolean[] a = new Boolean[arr.length];
        for (int i = 0; i < arr.length; i++) {
            a[i] = arr[i];
        }
        return a;
    }

    public static Character[] mapChar(char... arr) {
        Character[] a = new Character[arr.length];
        for (int i = 0; i < arr.length; i++) {
            a[i] = arr[i];
        }
        return a;
    }

}
