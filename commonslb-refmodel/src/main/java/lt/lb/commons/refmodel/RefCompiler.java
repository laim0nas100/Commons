package lt.lb.commons.refmodel;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.stream.Stream;
import lt.lb.commons.F;
import lt.lb.commons.Nulls;

/**
 *
 * @author laim0nas100
 */
public class RefCompiler {

    public static final int DEFAULT_COMPILE_DEPTH = 7;

    public static <T extends Ref & RefModel> T compile(Class<T> rootCls) throws InstantiationException, IllegalAccessException, NoSuchMethodException, IllegalArgumentException, InvocationTargetException {
        return compileRoot(DEFAULT_COMPILE_DEPTH, rootCls, RefNotation.DEFAULT);
    }

    public static <T extends Ref & RefModel> T compileRoot(int limit, Class<T> rootCls, RefNotation notation) throws InstantiationException, IllegalAccessException, NoSuchMethodException, IllegalArgumentException, InvocationTargetException {
        Nulls.requireNonNulls(rootCls, notation);
        Ref root = rootCls.getDeclaredConstructor().newInstance();
        root.local = "";
        root.relative = "";
        root.notation = notation;
        compile(root, "", limit, notation);
        return (T) root;
    }

    public static <M, T extends Ref & RefList<M>> T compileRootList(int limit, Class<T> rootCls, Class<M> memberType, RefNotation notation) throws InstantiationException, IllegalAccessException, NoSuchMethodException, IllegalArgumentException, InvocationTargetException {
        Nulls.requireNonNulls(rootCls, memberType, notation);
        Ref root = rootCls.getDeclaredConstructor().newInstance();
        root.local = "";
        root.relative = "";
        root.notation = notation;
        root.parameterTypes = new Class[]{memberType};
        compileList(F.cast(root), limit, notation);
        return (T) root;
    }

    /**
     * Continue path compilation after accessing the {@link RefList##at(int) }
     * method, must pass a cloned member continuation, because the local and
     * relative paths are changed, and so all the nested children strings are
     * mangled.
     *
     * @param <T>
     * @param memberClone
     * @param index
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws NoSuchMethodException
     * @throws IllegalArgumentException
     * @throws InvocationTargetException
     */
    public static <T extends Ref> void compileContinuation(T memberClone, int index) throws InstantiationException, IllegalAccessException, NoSuchMethodException, IllegalArgumentException, InvocationTargetException {

        memberClone.local = memberClone.getNotation().getArrayIndexReplaced(memberClone.local, index);
        memberClone.relative = memberClone.getNotation().getArrayIndexReplaced(memberClone.relative, index);
        compile(memberClone, memberClone.relative, memberClone.compileLeft, memberClone.notation);
    }

    /**
     * Compile a list container, itself must be compiled with present paths,
     * unless it's the starting node.
     *
     * @param <T>
     * @param listContainer
     * @param compileDepthLeft
     * @param notation
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     * @throws InstantiationException
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     */
    public static <T extends Ref & RefList> void compileList(T listContainer, int compileDepthLeft, RefNotation notation) throws IllegalArgumentException, IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException {

        if (listContainer.parameterTypes.length != 1) {
            throw new IllegalArgumentException("Expected to have 1 parameter type of concrete class");
        }

        Class memberType = listContainer.parameterTypes[0];

        if (Ref.class.isAssignableFrom(memberType)) {
            Ref member = F.cast(memberType.getDeclaredConstructor().newInstance());
            member.local = notation.getArrayIndexTemplate();
            member.relative = notation.produceArrayAccess(listContainer.relative, member.local);
            member.notation = notation;
            member.compileLeft = compileDepthLeft;
            listContainer.memberContinuation = member;
            compile(member, member.relative, compileDepthLeft, notation);
        }
    }

    private static Ref initRef(Field field, String parent, int compileDepthLeft, RefNotation notation) throws NoSuchMethodException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        Class<?> type = field.getType();
        Ref ref = (Ref) type.getDeclaredConstructor().newInstance();
        ref.local = field.getName();
        ref.relative = notation.produceRelation(parent, ref.local);
        ref.notation = notation;
        ref.compileLeft = compileDepthLeft;
        Type genericType = field.getGenericType();
        if (genericType instanceof ParameterizedType) {
            ParameterizedType pt = F.cast(genericType);
            Type[] typeArgs = pt.getActualTypeArguments();
            ref.parameterTypes = Stream.of(typeArgs)
                    .filter(t -> t instanceof Class)
                    .toArray(s -> new Class[s]);
        }
        return ref;
    }

    private static void compile(Ref me, String parentRelative, int compileDepthLeft, RefNotation notation) throws IllegalArgumentException, IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException {
        if (compileDepthLeft <= 0) {
            return;
        }
        compileDepthLeft--;
        for (Field field : me.getClass().getFields()) {
            Class<?> type = field.getType();
            if (!Ref.class.isAssignableFrom(type)) {
                continue;
            }
            Ref ref = initRef(field, parentRelative, compileDepthLeft, notation);
            field.set(me, ref);

            boolean isList = RefList.class.isAssignableFrom(type);
            boolean isModel = RefModel.class.isAssignableFrom(type);
            if (isModel && isList) {
                throw new IllegalStateException(
                        String.format("%s implements both %s %s", type.getName(), RefList.class.getName(), RefModel.class.getName())
                );
            }

            if (isList) {
                compileList(F.cast(ref), compileDepthLeft, notation);
            } else if (isModel) {//recursive continuation
                compile(ref, ref.getRelative(), compileDepthLeft, notation);
            }
        }
    }
}
