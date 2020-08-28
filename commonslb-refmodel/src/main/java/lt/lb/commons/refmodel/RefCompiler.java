package lt.lb.commons.refmodel;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

/**
 *
 * @author laim0nas100
 */
public class RefCompiler {

    public static final int DEFAULT_COMPILE_DEPTH = 5;
    public static final String DEFAULT_SEPARATOR = ".";

    public static <T extends Ref> T compile(Class<T> rootCls) throws InstantiationException, IllegalAccessException, NoSuchMethodException, IllegalArgumentException, InvocationTargetException {
        return compile(DEFAULT_COMPILE_DEPTH, rootCls, DEFAULT_SEPARATOR);
    }

    public static <T extends Ref> T compile(int limit, Class<T> rootCls, String sep) throws InstantiationException, IllegalAccessException, NoSuchMethodException, IllegalArgumentException, InvocationTargetException {
        Ref root = rootCls.getDeclaredConstructor().newInstance();
        root.local = "";
        root.relative = "";
        if (RefModel.class.isAssignableFrom(rootCls)) {
            compile(root, "", limit, sep);
        }

        return (T) root;
    }

    private static void compile(Ref me, String parentRelative, int limit, String separator) throws IllegalArgumentException, IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException {
        if (limit <= 0) {
            return;
        }
        String substring = "";
        if (!parentRelative.isEmpty()) {
            substring = parentRelative + separator;
        }
        Class cls = me.getClass();

        for (Field f : cls.getFields()) {
            Class<?> type = f.getType();

            if (Ref.class.isAssignableFrom(type)) {
                Ref ref = (Ref) type.getDeclaredConstructor().newInstance();
                f.set(me, ref);
                ref.local = f.getName();
                ref.relative = substring + ref.local;
                if (RefModel.class.isAssignableFrom(type)) {
                    compile(ref, ref.relative, limit - 1, separator);
                }
            }
        }
    }
}
