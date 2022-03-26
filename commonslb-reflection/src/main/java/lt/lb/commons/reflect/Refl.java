package lt.lb.commons.reflect;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Objects;
import lt.lb.commons.Nulls;
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
}
