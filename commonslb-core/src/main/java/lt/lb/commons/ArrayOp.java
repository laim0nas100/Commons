/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lt.lb.commons;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;

/**
 *
 * @author Laimonas-Beniusis-PC
 */
public class ArrayOp {

    public static <T> T[] merge(T[] one, T... two) {
        LinkedList<T> list = new LinkedList<>();
        for (T t : one) {
            list.add(t);
        }
        for (T t : two) {
            list.add(t);
        }

        Class<T> componentType = (Class<T>) one.getClass().getComponentType();
        return newArray(list, componentType);

    }

    public static <T> T[] merge(T[] one, T[]... two) {
        LinkedList<T> list = new LinkedList<>();
        for (T t : one) {
            list.add(t);
        }
        for (T[] arr : two) {
            for (T t : arr) {
                list.add(t);
            }
        }
        Class<T> componentType = (Class<T>) one.getClass().getComponentType();
        return newArray(list, componentType);

    }

    public static <T> T[] castArray(Object[] array, Class<T> clz) {
        T[] a = makeArray(array.length, clz);
        for (int i = 0; i < array.length; i++) {
            a[i] = (T) array[i];
        }
        return a;
    }

    public static <T> T[] makeArray(int size, Class<T> clz) {
        if (clz.isPrimitive()) {
            throw new IllegalArgumentException("Primitives are not supported");
        }
        T[] a = (T[]) java.lang.reflect.Array.newInstance(clz, size);
        return a;
    }

    public static <T> T[] newArray(List<T> list, Class<T> clz) {
        return list.toArray(makeArray(list.size(), clz));
    }

    public static Object[] newArray(List list) {
        return newArray(list, Object.class);
    }

    public static <T> T[] remove(T[] one, T... two) {
        LinkedList<T> list = new LinkedList<>();
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
        LinkedList<T> list = new LinkedList<>();
        HashSet<Integer> set = new HashSet<>();
        for (int index : two) {
            set.add(index);
        }
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
        LinkedList<T> list = new LinkedList<>();

        for (int i = 0; i < one.length; i++) {

            if (i == where) {
                for (T t : what) {
                    list.add(t);
                }
            } else {
                list.add(one[i]);
            }

        }
        Class<T> componentType = (Class<T>) one.getClass().getComponentType();
        return newArray(list, componentType);

    }

    public static <T> int count(Predicate<T> test, T... array) {
        int count = 0;
        for (T t : array) {
            if (test.test(t)) {
                count++;
            }
        }
        return count;
    }

}
