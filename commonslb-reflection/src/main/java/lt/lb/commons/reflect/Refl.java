package lt.lb.commons.reflect;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lt.lb.commons.LineStringBuilder;
import lt.lb.commons.Nulls;
import lt.lb.commons.iteration.streams.SimpleStream;
import lt.lb.commons.reflect.unified.IObjectField;
import lt.lb.commons.reflect.unified.ReflFields;
import lt.lb.uncheckedutils.NestedException;
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

        @Override
        public int hashCode() {
            return selfHashcode(this);
        }

        @Override
        public boolean equals(Object obj) {
            return selfEquals(this, obj);
        }

        @Override
        public String toString() {
            return selfToString(this);
        }
    }

    public static String selfToString(Object ob) {
        try {
            return allFieldsToString(ob, true);
        } catch (IllegalArgumentException | IllegalAccessException ex) {
            if (ob == null) {
                return "null";
            }
            return ob.getClass().getSimpleName() + System.identityHashCode(ob);
        }
    }

    public static String allFieldsToString(Object obj, boolean ignoreExceptions) throws IllegalArgumentException, IllegalAccessException {
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
                if (!ignoreExceptions) {
                    if (ex instanceof IllegalAccessException) {
                        throw (IllegalAccessException) ex;
                    }
                    if (ex instanceof IllegalArgumentException) {
                        throw (IllegalArgumentException) ex;
                    }
                    throw NestedException.of(ex);
                }

            }
        }

        if (!fieldsOf.isEmpty()) {
            sb.removeFromEnd(2);
        }
        return sb.append("}").toString();

    }

    public static boolean selfEquals(Object o1, Object o2) {
        return reflectiveEquals(o1, o2, true);
    }

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

        Map<Field, IObjectField> map1 = o1Fields.toMap(m -> m.field(), m -> m);
        Map<Field, IObjectField> map2 = o2Fields.toMap(m -> m.field(), m -> m);

        if (map1.size() != map2.size()) {
            return false;
        }

        if(!map2.keySet().containsAll(map1.keySet())){
            return false;
        }

        for (Field f2 : map2.keySet()) {
            IObjectField field = map1.getOrDefault(f2, null);
            if (field == null) {
                return false;
            }
            try {
                if (!Objects.equals(field.get(o1), field.get(o2))) {
                    return false;
                }
            } catch (IllegalAccessException | IllegalArgumentException ex) {
                if (!ignoreExceptions) {
                    throw NestedException.of(ex);
                }
            }
        }
        return true;
    }

    public static int selfHashcode(Object ob) {
        return reflectiveHashcode(ob, true);
    }

    public static int reflectiveHashcode(Object ob, boolean ignoreExceptions) {
        if (ob == null) {
            return 0;
        }
        Class cls = ob.getClass();
        List<IObjectField> fields = ReflFields.getLocalFields(cls).toList();

        int hash = 7;
        for (IObjectField f : fields) {
            try {
                hash = 59 * hash + Objects.hashCode(f.get(ob));
            } catch (IllegalAccessException | IllegalArgumentException ex) {
                if (!ignoreExceptions) {
                    throw NestedException.of(ex);
                }
            }
        }

        return hash;

    }

}
