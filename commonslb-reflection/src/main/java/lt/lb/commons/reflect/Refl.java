package lt.lb.commons.reflect;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;
import lt.lb.commons.func.unchecked.UncheckedFunction;

/**
 * Reflection methods
 * @author laim0nas100
 */
public class Refl {

    public static void doClassSuperIteration(Class node, Consumer<Class> cons) {
        while (node != null) {
            cons.accept(node);
            node = node.getSuperclass();
        }
    }
    
    public static LinkedList<Field> getFieldsOf(Class cls, Predicate<Field> f) {
        LinkedList<Field> list = new LinkedList<>();
        doClassSuperIteration(cls, n -> Stream.of(n.getDeclaredFields())
                .filter(f)
                .forEach(list::add)
        );
        return list;
    }

    public static LinkedList<Method> getMethodsOf(Class cls, Predicate<Method> f) {
        LinkedList<Method> list = new LinkedList<>();
        doClassSuperIteration(cls, n -> Stream.of(n.getDeclaredMethods())
                .filter(f)
                .forEach(list::add)
        );
        return list;
    }

    public static <T> T fieldAccessableDo(Field field, UncheckedFunction<Field, T> function) throws Throwable {
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
        return Refl.fieldAccessableDo(field, f -> f.get(object));
    }

    public static void fieldAccessableSet(Field field, Object object, Object value) throws Throwable {
        Refl.fieldAccessableDo(field, f -> {
            f.set(object, value);
            return null;
        });
    }

    public static <T> T methodAccessableDo(Method method, UncheckedFunction<Method, T> func) throws Throwable {
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
        return Refl.methodAccessableDo(method, m -> m.invoke(target, args));
    }
}
