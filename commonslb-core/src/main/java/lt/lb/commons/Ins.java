package lt.lb.commons;

import java.util.Objects;

/**
 *
 * Dynamic instanceOf operation, with either Class or Object (but not both)
 *
 * @author laim0nas100
 */
public class Ins<T> {

    public static class InsCl<T> extends Ins<T> {

        private InsCl(Class<T> cl, T ob) {
            super(cl, ob);
            if (cl == null) {
                throw new IllegalArgumentException("Class must be provided");
            }
        }

        /**
         * Delegates to {@link #superClassOfAll(InsCl)},
         *
         * @param cls
         * @return
         */
        public boolean superClassOf(Class cls) {
            return superClassOfAll(cls);
        }
        
        /**
         * Delegates to {@link #superClassOfAll(InsCl)},
         *
         * @param obj
         * @return
         */
        public boolean superClassOf(Object obj) {
            return superClassOfAll(obj);
        }

        /**
         * Checks whether contained class is a superClass of all given objects.
         * Accepts null. Empty array results in {@code false}
         *
         * @param objs
         * @return
         */
        public boolean superClassOfAll(Object... objs) {
            if (objs == null) {
                return isNull;
            }

            if (objs.length == 0) {
                return false;
            }
            if (isNull) {
                return allNull(objs, true);
            }

            for (Object c : objs) {
                if(!Ins.instanceOf(c, clazz)){
                    return false;
                }
            }
            return true;
        }

        /**
         * Checks whether contained class is a superClass of any given objects.
         * Accepts null. Empty array results in {@code false}
         *
         * @param objs
         * @return
         */
        public boolean superClassOfAny(Object... objs) {
            if (objs == null) {
                return isNull;
            }

            if (objs.length == 0) {
                return false;
            }
            if (isNull) {
                return anyNull(objs, true);
            }

            for (Object c : objs) {
                if(Ins.instanceOf(c, clazz)){
                    return true;
                }
            }
            return false;
        }

        /**
         * Checks whether contained class is a superClass of all given classes.
         * Accepts null. Empty array results in {@code false}
         *
         * @param cls
         * @return
         */
        public boolean superClassOfAll(Class... cls) {
            if (cls == null) {
                return isNull;
            }

            if (cls.length == 0) {
                return false;
            }
            if (isNull) {
                return allNull(cls, true);
            }

            for (Class c : cls) { // was are the of now
                if (!Ins.instanceOfClass(c, clazz)) {
                    return false;
                }
            }
            return true;
        }

        /**
         * Checks whether contained class are an superClass of any given classes.
         * Accepts null. Empty array results in {@code false}
         *
         * @param cls
         * @return
         */
        public boolean superClassOfAny(Class... cls) {
            if (cls == null) {
                return isNull;
            }

            if (cls.length == 0) {
                return false;
            }
            if (isNull) {
                return anyNull(cls, true);
            }

            for (Class c : cls) { // was are the of now
                if (Ins.instanceOfClass(c, clazz)) {
                    return true;
                }
            }
            return false;
        }

    }

    protected final Class<T> clazz;
    protected final T object;
    protected final boolean isNull;

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
    public static <T> InsCl<T> of(Class<T> cls) {
        Objects.requireNonNull(cls, "Null should not be a class parameter");
        return new InsCl(cls, null);
    }

    /**
     * Create {@code Ins} of given object
     *
     * @param <T>
     * @param obj
     * @return
     */
    public static <T> Ins<T> ofNullable(T obj) {
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
     * Checks whether contained object or class are an instance of all given
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
            return allNull(cls, true);
        }

        for (Class c : cls) {
            if (!instanceOf0(c)) {
                return false;
            }

        }
        return true;

    }

    /**
     * Checks whether contained object or class are an instance of any given
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
            return anyNull(cls, true);
        }

        for (Class c : cls) {
            if (instanceOf0(c)) {
                return true;
            }

        }
        return false;

    }

    protected boolean instanceOf0(Class c) {
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
            return false;
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
            return false;
        } else {
            return of.isInstance(what);
        }
    }

    protected boolean allNull(Object[] arr, boolean ifNull) {

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

    protected boolean anyNull(Object[] arr, boolean ifNull) {

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
