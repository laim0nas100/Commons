package lt.lb.commons;

import java.util.function.Supplier;

/**
 *
 * @author laim0nas100
 */
public abstract class Nulls {

    /**
     * ad hoc empty object to be used instead of null where null values are not supported.
     */
    public static final Object EMPTY_OBJECT = new Object() {
        @Override
        public String toString() {
            return "EMPTY_OBJECT";
        }
    };

    /**
     * Return true only if either of the arguments is null, similar to XOR.
     *
     *
     * @param first
     * @param second
     * @return
     */
    public static boolean eitherNull(Object first, Object second) {
        if (first == second) {
            return false;
        }
        return first == null || second == null;
    }

    /**
     * {@link Objects##isNull(java.lang.Object)}
     *
     * @param obj
     * @return
     */
    public static boolean isNull(Object obj) {
        return obj == null;
    }

    /**
     * {@link Objects##nonNull(java.lang.Object)}
     *
     * @param obj
     * @return
     */
    public static boolean nonNull(Object obj) {
        return obj != null;
    }

    /**
     * {@link Objects##requireNonNull(java.lang.Object, java.util.function.Supplier)}
     *
     * @param obj
     * @param supplier
     * @return
     */
    public static <T> T requireNonNullElseGet(T obj, Supplier<? extends T> supplier) {
        return (obj != null) ? obj
                : requireNonNull(requireNonNull(supplier, "supplier").get(), "supplier.get()");
    }

    /**
     * {@link Objects##requireNonNull(java.lang.Object, java.lang.Object)}
     *
     * @param obj
     * @param defaultObj
     * @return
     */
    public static <T> T requireNonNullElse(T obj, T defaultObj) {
        return (obj != null) ? obj : requireNonNull(defaultObj, "defaultObj");
    }

    /**
     * {@link Objects##requireNonNull(java.lang.Object,  java.util.function.Supplier)}
     *
     *
     * @param obj
     * @param messageSupplier
     * @return
     */
    public static <T> T requireNonNull(T obj, Supplier<String> messageSupplier) {
        if (obj == null) {
            throw new NullPointerException(messageSupplier == null
                    ? null : messageSupplier.get());
        }
        return obj;
    }

    /**
     * {@link Objects##requireNonNull(java.lang.Object,  java.lang.String)
     * }
     *
     * @param obj
     * @param message
     * @return
     */
    public static <T> T requireNonNull(T obj, String message) {
        if (obj == null) {
            throw new NullPointerException(message);
        }
        return obj;
    }

    /**
     * Checks if any of the objects are null and throws NPE with first found
     * null index message.
     *
     * @param objects
     */
    public static void requireNonNulls(Object... objects) {
        requireNonNull(objects, "Object array is null");
        for (int i = 0; i < objects.length; i++) {
            if (objects[i] == null) {
                throw new NullPointerException("Parameter at index:" + i + " is null");
            }
        }
    }

    /**
     * {@link Objects##requireNonNull(java.lang.Object)}
     *
     * @param obj
     * @return
     */
    public static <T> T requireNonNull(T obj) {
        if (obj == null) {
            throw new NullPointerException("Required parameter was null");
        }
        return obj;
    }

    /**
     * Checks if any of the objects are null.
     *
     * @param objects
     */
    public static <T> boolean anyNull(T... objects) {
        if (objects.length == 0) {
            return false;
        }
        return nullCheck(objects, true) >= 0;
    }

    /**
     * Checks if none of the objects are null.
     *
     * @param objects
     */
    public static <T> boolean noneNull(T... objects) {
        if (objects.length == 0) {
            return false;
        }
        return nullCheck(objects, true) < 0;
    }

    /**
     * Checks if all of the objects are null.
     *
     * @param objects
     */
    public static <T> boolean allNull(T... objects) {
        if (objects.length == 0) {
            return false;
        }
        return nullCheck(objects, false) < 0;
    }

    /**
     * Return first index where null or non-null item is found, otherwise return
     * -1.
     *
     * @param arr array of items to check for nulls
     * @param findNull true means to look for null, otherwise look for non-null
     * @return
     */
    public static int nullCheck(Object[] arr, final boolean findNull) {
        requireNonNull(arr, "Object array is null");
        for (int i = 0; i < arr.length; i++) {
            Object ob = arr[i];
            if ((findNull && ob == null) || (!findNull && ob != null)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Count null items. If the array is null return zero.
     *
     * @param <T>
     * @param objects
     * @return
     */
    public static <T> int countNull(T... objects) {
        if (objects == null) {
            return 0;
        }
        int count = 0;
        for (T item : objects) {
            count += item == null ? 1 : 0;
        }
        return count;
    }

    /**
     * If given object is null or is equal to {@link Nulls##EMPTY_OBJECT} then
     * return null, otherwise try to cast to given type.
     *
     * @param <T>
     * @param ob
     * @return
     * @throws ClassCastException
     */
    public static <T> T castOrNullIfEmptyObject(Object ob) throws ClassCastException {
        if (ob == null || EMPTY_OBJECT == ob) {
            return null;
        }
        return (T) ob;

    }

}
