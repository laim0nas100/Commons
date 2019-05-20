package lt.lb.commons.reflect;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 *
 * @author laim0nas100
 */
public class Refl {

    public static List<Field> getFieldsOf(Class cls, Predicate<Field> f) {
        Class node = cls;
        LinkedList<Field> list = new LinkedList<>();
        while (node != null) {
            Stream.of(node.getDeclaredFields()).filter(f).forEach(list::add);
            node = node.getSuperclass();
        }
        return list;
    }

    public static <T> Object fieldAccessableGet(Field field, Object object) throws Throwable {
        boolean inAccessible = !field.isAccessible();
        if (inAccessible) {
            field.setAccessible(true);
        }
        Object val = null;
        Throwable thr = null;
        try {
            val = field.get(object);
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

    public static <T> void fieldAccessableSet(Field field, Object object, Object value) throws Throwable {
        boolean inAccessible = !field.isAccessible();
        if (inAccessible) {
            field.setAccessible(true);
        }
        Throwable thr = null;
        try {
            field.set(object, value);
        } catch (Throwable th) {
            thr = th;
        }
        if (inAccessible) {
            field.setAccessible(false);
        }

        if (thr != null) {
            throw thr;
        }
    }
    
    public static Object invokeAccessable(Method method, Object target, Object[] args) throws Throwable{
        boolean inAccessible = !method.isAccessible();
        if (inAccessible) {
            method.setAccessible(true);
        }
        Throwable thr = null;
        Object result = null;
        try {
            result = method.invoke(target, args);
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
}