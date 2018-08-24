/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lt.lb.commons.refmodel;

import java.lang.reflect.Field;

/**
 *
 * @author Lemmin
 */
public class RefCompiler {

    public static final int DEFAULT_COMPILE_DEPTH = 5;
    public static final String separator = ".";

    public static <T extends Ref> T compile(Class<T> rootCls) throws InstantiationException, IllegalAccessException {
        return compile(DEFAULT_COMPILE_DEPTH, rootCls);
    }

    public static <T extends Ref> T compile(int limit, Class<T> rootCls) throws InstantiationException, IllegalAccessException {
        Ref root = rootCls.newInstance();
        root.local = "";
        root.relative = "";
        if (RefModel.class.isAssignableFrom(rootCls)) {
            compile(root, "", limit);
        }

        return (T) root;
    }

    private static void compile(Ref me, String parentRelative, int limit) throws IllegalArgumentException, IllegalAccessException, InstantiationException {
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
                Ref ref = (Ref) type.newInstance();
                f.set(me, ref);
                ref.local = f.getName();
                ref.relative = substring + ref.local;
                if (RefModel.class.isAssignableFrom(type)) {
                    compile(ref, ref.relative, limit - 1);
                }
            }
        }
    }
}
