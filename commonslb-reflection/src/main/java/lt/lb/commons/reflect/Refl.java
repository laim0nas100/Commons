package lt.lb.commons.reflect;

import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import lt.lb.commons.LineStringBuilder;
import lt.lb.commons.Nulls;
import lt.lb.commons.iteration.streams.MakeStream;
import lt.lb.commons.iteration.streams.SimpleStream;
import lt.lb.commons.reflect.unified.IObjectField;
import lt.lb.commons.reflect.unified.ReflFields;
import lt.lb.uncheckedutils.NestedException;
import lt.lb.uncheckedutils.SafeOpt;
import lt.lb.uncheckedutils.func.UncheckedFunction;

/**
 * Reflection methods
 *
 * @author laim0nas100
 */
public class Refl {

    public static <T> T fieldAccessableDo(Field field, UncheckedFunction<Field, T> function) throws Throwable {
        Nulls.requireNonNulls(field, function);
        boolean inAccessible = !field.isAccessible();
        if (inAccessible) {
            field.setAccessible(true);
        }
        T val = null;
        Throwable thr = null;
        try {
            val = function.applyUnchecked(field);
        } catch (Throwable th) {
            thr = th;
        }
        if (inAccessible) {
            field.setAccessible(false);
        }
        if (thr != null) {
            throw thr;
        }
        return val;
    }

    public static Object fieldAccessableGet(Field field, Object object) throws Throwable {
        return fieldAccessableDo(field, f -> f.get(object));
    }

    public static void fieldAccessableSet(Field field, Object object, Object value) throws Throwable {
        fieldAccessableDo(field, f -> {
            f.set(object, value);
            return null;
        });
    }

    public static <T> T methodAccessableDo(Method method, UncheckedFunction<Method, T> func) throws Throwable {
        Nulls.requireNonNulls(method, func);
        boolean inAccessible = !method.isAccessible();
        if (inAccessible) {
            method.setAccessible(true);
        }
        Throwable thr = null;
        T result = null;
        try {
            result = func.applyUnchecked(method);
        } catch (Throwable th) {
            thr = th;
        }
        if (inAccessible) {
            method.setAccessible(false);
        }
        if (thr != null) {
            throw thr;
        }
        return result;
    }

    public static Object invokeAccessable(Method method, Object target, Object[] args) throws Throwable {
        return Refl.methodAccessableDo(method, m -> invokeMethod(method, target, args));
    }

    /**
     * Invokes method with given arguments.
     *
     * Handles all exceptions. If method throws
     * {@link InvocationTargetException} then if the cause is
     * {@link RuntimeException} or {@link Error} it gets re-thrown. Otherwise
     * the method nests the cause within {@link NestedException}.
     *
     * If {@link IllegalArgumentException} or {@link IllegalArgumentException}
     * happened, it is also nested within {@link NestedException}.
     *
     * @param <T>
     * @param method
     * @param target
     * @param args
     * @return
     */
    public static <T> T invokeMethod(Method method, Object target, Object... args) {
        Objects.requireNonNull(method, "Given method is null");
        try {
            return (T) method.invoke(target, args);
        } catch (IllegalAccessException | IllegalArgumentException ex) {
            throw NestedException.of(ex);
        } catch (InvocationTargetException ex) {
            Throwable cause = ex.getCause();
            if (cause instanceof RuntimeException) {
                throw (RuntimeException) cause;
            }
            if (cause instanceof Error) {
                throw (Error) cause;
            }
            throw NestedException.of(cause);
        }
    }

    /**
     * Simple base class to abstract
     * {@link Object#hashCode()}, {@link Object#equals(java.lang.Object)}, {@link Object#toString()}
     * methods, for classes that are only relevant for their fields, uses
     * reflection to obtain all the information from fields.
     */
    public static abstract class SelfID {

        protected ThreadLocal<Boolean> inside_hash = ThreadLocal.withInitial(() -> false);

        @Override
        public int hashCode() {
            if (inside_hash.get()) {
                return 7;
            }
            inside_hash.set(true);
            try {
                return reflectiveHashCode(this, true);
            } finally {
                inside_hash.set(false);
            }
        }

        protected ThreadLocal<Boolean> inside_equals = ThreadLocal.withInitial(() -> false);

        @Override
        @SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
        public boolean equals(Object obj) {
            if (inside_equals.get()) {
                return this == obj;
            }
            inside_equals.set(true);
            try {
                return reflectiveEquals(this, obj, true);
            } finally {
                inside_equals.set(false);
            }
        }

        protected ThreadLocal<Boolean> inside_string = ThreadLocal.withInitial(() -> false);

        @Override
        public String toString() {
            if (inside_string.get()) {
                return "looped";
            }
            inside_string.set(true);
            try {
                return reflectiveToString(this, true);
            } finally {
                inside_string.set(false);
            }
        }
    }

    /**
     * Gathers all local fields, and creates a {@code String} representation.
     *
     * @param obj
     * @param ignoreExceptions
     * @return
     */
    public static String reflectiveToString(Object obj, boolean ignoreExceptions) {
        if (obj == null) {
            return "null";
        }
        LineStringBuilder sb = new LineStringBuilder();

        Class cls = obj.getClass();
        SimpleStream<IObjectField> regularFieldsOf = ReflFields.getLocalFields(cls);
        List<Field> fieldsOf = regularFieldsOf.map(m -> m.field()).toList();
        sb.append(cls.getSimpleName()).append("{");
        for (Field field : fieldsOf) {
            try {
                StringBuilder s = new StringBuilder();

                s.append(field.getName()).append("=");
                s.append(Refl.fieldAccessableGet(field, obj)).append(", ");

                // only append all or nothing
                sb.append(s);
            } catch (Throwable ex) {
                if (ex instanceof Error) {
                    throw (Error) ex;
                }
                if (ex instanceof RuntimeException) {
                    throw (RuntimeException) ex;
                }
                if (!ignoreExceptions) {
                    throw NestedException.of(ex);
                }

            }
        }

        if (!fieldsOf.isEmpty()) {
            sb.removeFromEnd(2);
        }
        return sb.append("}").toString();

    }

    /**
     * Gathers all the local fields of both objects and compares each of them to
     * establish equality.
     *
     * @param o1
     * @param o2
     * @param ignoreExceptions
     * @return
     */
    public static boolean reflectiveEquals(Object o1, Object o2, boolean ignoreExceptions) {
        if (o1 == o2) {
            return true;
        }
        if (o2 == null) {
            return false;
        }
        Class cl1 = o1.getClass();
        Class cl2 = o2.getClass();
        if (cl1 != cl2) {
            return false;
        }
        SimpleStream<IObjectField> o1Fields = ReflFields.getLocalFields(cl1);
        SimpleStream<IObjectField> o2Fields = ReflFields.getLocalFields(cl2);

        Set<Field> set1 = o1Fields.map(m -> m.field()).toSet();
        Set<Field> set2 = o2Fields.map(m -> m.field()).toSet();

        if (set1.size() != set2.size()) {
            return false;
        }

        if (!set2.containsAll(set1)) {
            return false;
        }

        for (Field field : set2) {
            if (!set1.contains(field)) {
                return false;
            }
            try {
                if (!Objects.equals(Refl.fieldAccessableGet(field, o1), Refl.fieldAccessableGet(field, o2))) {
                    return false;
                }
            } catch (Throwable ex) {
                if (ex instanceof Error) {
                    throw (Error) ex;
                }
                if (ex instanceof RuntimeException) {
                    throw (RuntimeException) ex;
                }
                if (!ignoreExceptions) {
                    throw NestedException.of(ex);
                }
            }
        }
        return true;
    }

    /**
     * Gathers all the local fields of both objects computes a hash code based
     * on them.
     *
     * @param ob
     * @param ignoreExceptions
     * @return
     */
    public static int reflectiveHashCode(Object ob, boolean ignoreExceptions) {
        if (ob == null) {
            return 0;
        }
        Class cls = ob.getClass();
        List<IObjectField> fields = ReflFields.getLocalFields(cls).toList();

        int hash = 7;
        for (IObjectField f : fields) {
            try {
                hash = 59 * hash + Objects.hashCode(Refl.fieldAccessableGet(f.field(), ob));
            } catch (Throwable ex) {
                if (ex instanceof Error) {
                    throw (Error) ex;
                }
                if (ex instanceof RuntimeException) {
                    throw (RuntimeException) ex;
                }
                if (!ignoreExceptions) {
                    throw NestedException.of(ex);
                }
            }
        }

        return hash;

    }

    public static SimpleStream<PropertyDescriptor> getPropertyDescriptors(Class sourceClass) {
        Nulls.requireNonNull(sourceClass);

        return MakeStream.fromValues(sourceClass)
                .mapSafeOpt(cls -> SafeOpt.ofGet(() -> Introspector.getBeanInfo(cls)))
                .flatMap(m -> MakeStream.from(m.getPropertyDescriptors()))
                .filter(Nulls::nonNull)
                .filter(p -> !p.getName().equals("class"));
    }

    /**
     * Simple base class to abstract
     * {@link Object#hashCode()}, {@link Object#equals(java.lang.Object)}, {@link Object#toString()}
     * methods, for classes that are only relevant for their properties, uses
     * bean {@link Introspector} to obtain all the information from properties.
     */
    public static abstract class SelfIDBean {

        protected ThreadLocal<Boolean> inside_hash = ThreadLocal.withInitial(() -> false);

        @Override
        public int hashCode() {
            if (inside_hash.get()) {
                return 7;
            }
            inside_hash.set(true);
            try {
                return beanHashCode(this, true);
            } finally {
                inside_hash.set(false);
            }
        }

        protected ThreadLocal<Boolean> inside_equals = ThreadLocal.withInitial(() -> false);

        @Override
        @SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
        public boolean equals(Object obj) {
            if (inside_equals.get()) {
                return this == obj;
            }
            inside_equals.set(true);
            try {
                return beanEquals(this, obj, true);
            } finally {
                inside_equals.set(false);
            }
        }

        protected ThreadLocal<Boolean> inside_string = ThreadLocal.withInitial(() -> false);

        @Override
        public String toString() {
            if (inside_string.get()) {
                return "looped";
            }
            inside_string.set(true);
            try {
                return beanToString(this, true);
            } finally {
                inside_string.set(false);
            }
        }
    }

    /**
     * Gathers all bean properties, and creates a {@code String} representation.
     *
     * @param obj
     * @param ignoreExceptions
     * @return
     */
    public static String beanToString(Object obj, boolean ignoreExceptions) {
        if (obj == null) {
            return "null";
        }
        LineStringBuilder sb = new LineStringBuilder();

        Class cls = obj.getClass();
        List<PropertyDescriptor> propertyDescriptors = getPropertyDescriptors(cls).toList();
        sb.append(cls.getSimpleName()).append("{");
        for (PropertyDescriptor p : propertyDescriptors) {
            try {

                Object value = Refl.invokeMethod(p.getReadMethod(), obj);
                StringBuilder s = new StringBuilder();
                s.append(p.getDisplayName()).append("=").append(value).append(", ");

                // only append all or nothing
                sb.append(s);
            } catch (Throwable ex) {
                if (ex instanceof Error) {
                    throw (Error) ex;
                }
                if (ex instanceof RuntimeException) {
                    throw (RuntimeException) ex;
                }
                if (!ignoreExceptions) {
                    throw NestedException.of(ex);
                }

            }
        }

        if (!propertyDescriptors.isEmpty()) {
            sb.removeFromEnd(2);
        }
        return sb.append("}").toString();

    }

    /**
     * Gathers all the bean properties of both objects and compares each of them
     * to establish equality.
     *
     * @param o1
     * @param o2
     * @param ignoreExceptions
     * @return
     */
    public static boolean beanEquals(Object o1, Object o2, boolean ignoreExceptions) {
        if (o1 == o2) {
            return true;
        }
        if (o2 == null) {
            return false;
        }

        Set<Method> set1 = getPropertyDescriptors(o1.getClass()).map(m -> m.getReadMethod()).toSet();
        Set<Method> set2 = getPropertyDescriptors(o2.getClass()).map(m -> m.getReadMethod()).toSet();
        if (set1.size() != set2.size()) {
            return false;
        }

        if (!set2.containsAll(set1)) {
            return false;
        }

        for (Method method : set2) {
            if (!set1.contains(method)) {
                return false;
            }
            try {
                if (!Objects.equals(Refl.invokeMethod(method, o1), Refl.invokeMethod(method, o2))) {
                    return false;
                }
            } catch (Throwable ex) {
                if (ex instanceof Error) {
                    throw (Error) ex;
                }
                if (ex instanceof RuntimeException) {
                    throw (RuntimeException) ex;
                }
                if (!ignoreExceptions) {
                    throw NestedException.of(ex);
                }
            }
        }
        return true;
    }

    /**
     * Gathers all the bean properties of both objects computes a hash code
     * based on them.
     *
     * @param ob
     * @param ignoreExceptions
     * @return
     */
    public static int beanHashCode(Object ob, boolean ignoreExceptions) {
        if (ob == null) {
            return 0;
        }
        Class cls = ob.getClass();
        List<PropertyDescriptor> props = getPropertyDescriptors(cls).toList();

        int hash = 7;
        for (PropertyDescriptor f : props) {
            try {
                hash = 59 * hash + Objects.hashCode(Refl.invokeMethod(f.getReadMethod(), ob));
            } catch (Throwable ex) {
                if (ex instanceof Error) {
                    throw (Error) ex;
                }
                if (ex instanceof RuntimeException) {
                    throw (RuntimeException) ex;
                }
                if (!ignoreExceptions) {
                    throw NestedException.of(ex);
                }
            }
        }

        return hash;

    }
}
