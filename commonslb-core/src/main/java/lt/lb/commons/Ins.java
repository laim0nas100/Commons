package lt.lb.commons;

import java.util.Objects;

/**
 *
 * Dynamic instanceOf operation, with either Class or Object (but not both)
 *
 * @author laim0nas100
 */
public class Ins<T> {

    private final Class<T> clazz;
    private final T object;
    private boolean isNull = false;

    private Ins(Class<T> cl, T ob) {
        this.clazz = cl;
        this.object = ob;
        this.isNull = object == null && clazz == null;

        if (clazz != null && ob != null) {
            throw new IllegalArgumentException("Can only provide one or the other");
        }
    }

    /**
     * Create {@code Ins} of given class
     *
     * @param <T>
     * @param cls
     * @return
     */
    public static <T> Ins<T> of(Class<T> cls) {
        return new Ins(cls, null);
    }

    /**
     * Create {@code Ins} of given object
     *
     * @param <T>
     * @param obj
     * @return
     */
    public static <T> Ins<T> of(T obj) {
        return new Ins<>(null, obj);
    }

    /**
     * Delegates to {@link #instanceOfAll(Ins)},
     *
     * @param cls
     * @return
     */
    public boolean instanceOf(Class cls) {
        return instanceOfAll(cls);
    }

    /**
     * Checks wether contained object or class are an instance of all given
     * classes. Accepts null. Empty array results in {@code false}
     *
     * @param cls
     * @return
     */
    public boolean instanceOfAll(Class... cls) {
        if (cls == null) {
            return isNull;
        }

        if (cls.length == 0) {
            return false;
        }
        if (isNull) {
            return all(cls, true);
        }

        for (Class c : cls) {
            if (!instanceOf0(c)) {
                return false;
            }

        }
        return true;

    }

    /**
     * Checks wether contained object or class are an instance of any given
     * class. Accepts null. Empty array results in {@code false}
     *
     * @param cls
     * @return
     */
    public boolean instanceOfAny(Class... cls) {
        if (cls == null) {
            return isNull;
        }
        if (cls.length == 0) {
            return false;
        }
        if (isNull) {
            return any(cls, true);
        }

        for (Class c : cls) {
            if (instanceOf0(c)) {
                return true;
            }

        }
        return false;

    }

    private boolean instanceOf0(Class c) {
        if (clazz == null) {
            return instanceOf(object, c);
        } else {
            return instanceOfClass(clazz, c);
        }

    }

    /**
     * Null-friendly {@code Class.isAssignableFrom} version.
     *
     * @param what
     * @param of
     *
     * Static equivalent: what instanceof of
     * @return
     */
    public static boolean instanceOfClass(Class what, Class of) {
        if (of == null) { // nothing except null is instance of null
            return what == null;
        }
        if (what == null) {
            return Object.class.isAssignableFrom(of); // null is an instance of every object
        } else {
            return of.isAssignableFrom(what);
        }
    }

    /**
     * Null-friendly {@code Class.isInstance} version.
     *
     * @param what
     * @param of
     *
     * Static equivalent: what instanceof of
     * @return
     */
    public static boolean instanceOf(Object what, Class of) {
        if (of == null) { // nothing except null is instance of null
            return what == null;
        }
        if (what == null) {
            return Object.class.isAssignableFrom(of); // null is an instance of every object except for primitives
        } else {
            return of.isInstance(what);
        }
    }

    private boolean all(Object[] arr, boolean ifNull) {

        for (int i = 0; i < arr.length; i++) {
            if (ifNull) {
                if (arr[i] != null) {
                    return false;
                }
            } else {
                if (arr[i] == null) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean any(Object[] arr, boolean ifNull) {

        for (int i = 0; i < arr.length; i++) {
            if (ifNull) {
                if (arr[i] == null) {
                    return true;
                }
            } else {
                if (arr[i] != null) {
                    return true;
                }
            }
        }
        return false;
    }

}
