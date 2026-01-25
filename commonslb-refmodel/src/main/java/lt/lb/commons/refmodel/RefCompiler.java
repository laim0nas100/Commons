package lt.lb.commons.refmodel;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.stream.Stream;
import lt.lb.commons.F;

/**
 *
 * @author laim0nas100
 */
public class RefCompiler {

    public static final int DEFAULT_COMPILE_DEPTH = 10;
    public static final String DEFAULT_SEPARATOR = ".";

    public static <T extends Ref> T compile(Class<T> rootCls) throws InstantiationException, IllegalAccessException, NoSuchMethodException, IllegalArgumentException, InvocationTargetException {
        return compile(DEFAULT_COMPILE_DEPTH, rootCls, DEFAULT_SEPARATOR);
    }

    public static <T extends Ref> T compile(int limit, Class<T> rootCls, String sep) throws InstantiationException, IllegalAccessException, NoSuchMethodException, IllegalArgumentException, InvocationTargetException {
        Ref root = rootCls.getDeclaredConstructor().newInstance();
        root.local = "";
        root.relative = "";
        root.separator = sep;
        if (RefModel.class.isAssignableFrom(rootCls)) {
            compile(root, "", limit, sep);
        }

        return (T) root;
    }

    public static <T extends Ref> void compileContinuation(Ref memberClone, int index) throws InstantiationException, IllegalAccessException, NoSuchMethodException, IllegalArgumentException, InvocationTargetException {

        memberClone.local = String.format(memberClone.local, index);
        memberClone.relative = String.format(memberClone.relative, index);
        compile(memberClone, memberClone.relative, memberClone.compileLeft, memberClone.separator);
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
        cls.getFields();

        for (Field field : cls.getFields()) {
            Class<?> type = field.getType();

            if (Ref.class.isAssignableFrom(type)) {
                Ref ref = (Ref) type.getDeclaredConstructor().newInstance();
                field.set(me, ref);
                ref.local = field.getName();
                ref.relative = substring + ref.local;
                ref.separator = separator;
                ref.compileLeft = limit - 1;
                Type genericType = field.getGenericType();
                if (genericType instanceof ParameterizedType) {
                    ParameterizedType pt = F.cast(genericType);
                    Type[] typeArgs = pt.getActualTypeArguments();
                    ref.parameterTypes = Stream.of(typeArgs)
                            .filter(t -> t instanceof Class)
                            .toArray(s -> new Class[s]);
                }
                boolean isList = RefList.class.isAssignableFrom(type);
                boolean isModel = RefModel.class.isAssignableFrom(type);
                if (isModel && isList) {
                    throw new IllegalStateException(
                            String.format("%s implements both %s %s", type.getName(), RefList.class.getName(), RefModel.class.getName())
                    );
                }

                if (isList) {
                    if (ref.parameterTypes.length != 1) {
                        throw new IllegalArgumentException(String.format("Expected %s to have 1 parameter type of concrete class", type.getName()));
                    }

                    Class memberType = ref.parameterTypes[0];

                    if (Ref.class.isAssignableFrom(memberType)) {
                        Ref member = F.cast(memberType.getDeclaredConstructor().newInstance());
                        member.local = "[%d]";
                        member.relative = ref.relative + member.local;
                        member.separator = separator;
                        member.compileLeft = limit - 1;
                        ref.memberContinuation = member;
                        compile(member, member.relative, limit - 1, separator);
                    } 

                } else if (isModel) {
                    compile(ref, ref.relative, limit - 1, separator);
                }
            }
        }
    }
}
