package lt.lb.commons;

import java.util.Objects;
import lt.lb.commons.misc.ExtComparable;
import lt.lb.commons.misc.ExtComparator;

/**
 *
 * Dynamic instanceOf operation, with either Class or Object (but not both)
 *
 * @author laim0nas100
 */
public class Ins<T> {

    public static class InsCl<T> extends Ins<T> implements ExtComparable<Class> {

        protected InsCl(boolean primitivePromotion, Class<T> cl, T ob) {
            super(primitivePromotion, cl, ob);
            if (cl == null) {
                throw new IllegalArgumentException("Class must be provided");
            }

        }

        @Override
        public int compareTo(Class o) {
            return Ins.typeComparator.compare(clazz, o);
        }

        @Override
        public Class get() {
            return clazz;
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
                if (!Ins.instanceOf(c, clazz)) {
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
                if (Ins.instanceOf(c, clazz)) {
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
         * Checks whether contained class are an superClass of any given
         * classes. Accepts null. Empty array results in {@code false}
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
    protected final boolean primitivePromotion;

    protected Ins(boolean primitivePromotion, Class<T> cl, T ob) {
        this.clazz = cl;
        this.object = ob;
        this.isNull = object == null && clazz == null;
        this.primitivePromotion = primitivePromotion;
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
        return new InsCl(false, cls, null);
    }

    /**
     * Create {@code Ins} of given class with primitive promotion
     *
     * @param <T>
     * @param cls
     * @return
     */
    public static <T> InsCl<T> ofPrimitivePromotion(Class<T> cls) {
        Objects.requireNonNull(cls, "Null should not be a class parameter");
        return new InsCl(true, cls, null);
    }

    /**
     * Create {@code Ins} of given object
     *
     * @param <T>
     * @param obj
     * @return
     */
    public static <T> Ins<T> ofNullable(T obj) {
        return new Ins<>(false, null, obj);
    }

    /**
     * Create {@code Ins} of given object with primitive promotion
     *
     * @param <T>
     * @param obj
     * @return
     */
    public static <T> Ins<T> ofNullablePrimitivePromotion(T obj) {
        return new Ins<>(true, null, obj);
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

    /**
     * Test to match type exactly
     *
     * @param cls
     * @return
     */
    public boolean exactly(Class cls) {
        if (cls == null) {
            return isNull;
        }

        if (isNull) {
            return false;
        }
        if (clazz == null) {
            return object.getClass().equals(cls);
        } else {
            return clazz.equals(cls);
        }

    }

    protected boolean instanceOf0(Class c) {
        if (clazz == null) {
            return primitivePromotion ? instanceOfPrimitivePromotion(object, c) : instanceOf(object, c);
        } else {
            return primitivePromotion ? instanceOfClassPrimitivePromotion(clazz, c) : instanceOfClass(clazz, c);
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
     * Null-friendly {@code Class.isAssignableFrom} version with primitive
     * promotion.
     *
     * @param what
     * @param of
     *
     * Static equivalent: what instanceof of
     * @return
     */
    public static boolean instanceOfClassPrimitivePromotion(Class what, Class of) {
        if (of == null) { // nothing except null is instance of null
            return what == null;
        }
        if (what == null) {
            return false;
        } else {
            return primitivePromotion(of).isAssignableFrom(what) || primitiveDemotion(of).isAssignableFrom(what);
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

    /**
     * Null-friendly {@code Class.isInstance} version with primitive promotion.
     *
     * @param what
     * @param of
     *
     * Static equivalent: what instanceof of
     * @return
     */
    public static boolean instanceOfPrimitivePromotion(Object what, Class of) {
        if (of == null) { // nothing except null is instance of null
            return what == null;
        }
        if (what == null) {
            return false;
        } else {
            return primitivePromotion(of).isInstance(what) || primitiveDemotion(of).isInstance(what);
        }
    }

    /**
     * Promote given primitive class to a non-primitive counterpart if possible,
     * otherwise return the argument. Null-safe.
     *
     * @param type
     * @return
     */
    public static Class primitivePromotion(Class type) {
        if (type == null) {
            return null;
        }

        if (!type.isPrimitive()) {
            return type;
        }

        if (Boolean.TYPE.equals(type)) {
            return Boolean.class;
        }
        if (Character.TYPE.equals(type)) {
            return Character.class;
        }

        if (Integer.TYPE.equals(type)) {
            return Integer.class;
        }

        if (Long.TYPE.equals(type)) {
            return Long.class;
        }

        if (Float.TYPE.equals(type)) {
            return Float.class;
        }

        if (Double.TYPE.equals(type)) {
            return Double.class;
        }

        if (Byte.TYPE.equals(type)) {
            return Byte.class;
        }

        if (Short.TYPE.equals(type)) {
            return Short.class;
        }

        if (Void.TYPE.equals(type)) {
            return Void.class;
        }

        return type;

    }

    /**
     * Demote given non-primitive class to a primitive counterpart if possible,
     * otherwise return the argument. Null-safe.
     *
     * @param type
     * @return
     */
    public static Class primitiveDemotion(Class type) {
        if (type == null) {
            return null;
        }

        if (type.isPrimitive()) {
            return type;
        }

        if (Boolean.class.equals(type)) {
            return Boolean.TYPE;
        }

        if (Character.class.equals(type)) {
            return Character.TYPE;
        }

        if (Integer.class.equals(type)) {
            return Integer.TYPE;
        }

        if (Long.class.equals(type)) {
            return Long.TYPE;
        }

        if (Float.class.equals(type)) {
            return Float.TYPE;
        }

        if (Double.class.equals(type)) {
            return Double.TYPE;
        }

        if (Byte.class.equals(type)) {
            return Byte.TYPE;
        }

        if (Short.class.equals(type)) {
            return Short.TYPE;
        }

        if (Void.class.equals(type)) {
            return Void.TYPE;
        }

        return type;

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

    /**
     * Comparator of types. Broader types (like {@code Object}) come first. Null
     * parameters comes first.
     */
    public static final ExtComparator<Class> typeComparator = new ExtComparator<Class>() {
        @Override
        public int compare(Class o1, Class o2) {
            if (o1 == null && o2 == null) {
                return 0;
            }

            if (o1 == null) {
                return -1;
            }
            
            if (o2 == null) {
                return 1;
            }
            
            boolean i_1 = instanceOfClass(o1, o2);
            boolean i_2 = instanceOfClass(o2, o1);
            if (i_1 && !i_2) {
                return 1;
            }
            if (!i_1 && i_2) {
                return -1;
            }

            return 0;
        }

    };

}
