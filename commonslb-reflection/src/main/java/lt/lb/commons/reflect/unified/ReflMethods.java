package lt.lb.commons.reflect.unified;

import java.beans.IntrospectionException;
import java.util.stream.Stream;
import lt.lb.commons.Nulls;
import lt.lb.commons.iteration.streams.SimpleStream;

/**
 *
 * @author laim0nas100
 */
public abstract class ReflMethods extends ReflBase {

    public static <S, T> SimpleStream<IMethod<S, T>> getMethods(Class<S> sourceClass) {
        Nulls.requireNonNulls(sourceClass);
        return doClassInheritanceStream(sourceClass, n -> Stream.of(n.getDeclaredMethods()))
                .map(m -> ReflBase.makeMethod(m));
    }

    public static <S, T> SimpleStream<IMethod<S, T>> getMethods(Class<S> sourceClass, Class<T> typeClass) {
        Nulls.requireNonNulls(sourceClass, typeClass);
        return doClassInheritanceStream(sourceClass, n -> Stream.of(n.getDeclaredMethods()))
                .map(m -> (IMethod<S, T>) ReflBase.makeMethod(m))
                .filter(m -> m.isReturnTypeOf(typeClass));

    }

    public static <S, T> SimpleStream<IStaticMethod<S, T>> getStaticMethods(Class<S> sourceClass) {
        Nulls.requireNonNulls(sourceClass);
        return doClassInheritanceStream(sourceClass, n -> Stream.of(n.getDeclaredMethods()))
                .map(m -> (IStaticMethod<S, T>) ReflBase.makeStaticMethod(m))
                .filter(Nulls::nonNull);

    }

    public static <S, T> SimpleStream<IStaticMethod<S, T>> getStaticMethods(Class<S> sourceClass, Class<T> typeClass) {
        Nulls.requireNonNulls(sourceClass, typeClass);
        return doClassInheritanceStream(sourceClass, n -> Stream.of(n.getDeclaredMethods()))
                .map(m -> (IStaticMethod<S, T>) ReflBase.makeStaticMethod(m))
                .filter(Nulls::nonNull)
                .filter(m -> m.isReturnTypeOf(typeClass));

    }

    public static <S, T> SimpleStream<IObjectMethod<S, T>> getLocalMethods(Class<S> sourceClass) {
        Nulls.requireNonNulls(sourceClass);
        return doClassInheritanceStream(sourceClass, n -> Stream.of(n.getDeclaredMethods()))
                .map(m -> (IObjectMethod<S, T>) ReflBase.makeObjectMethod(m))
                .filter(Nulls::nonNull);

    }

    public static <S, T> SimpleStream<IObjectMethod<S, T>> getLocalMethods(Class<S> sourceClass, Class<T> typeClass) {
        Nulls.requireNonNulls(sourceClass, typeClass);
        return doClassInheritanceStream(sourceClass, n -> Stream.of(n.getDeclaredMethods()))
                .map(m -> (IObjectMethod<S, T>) ReflBase.makeObjectMethod(m))
                .filter(Nulls::nonNull)
                .filter(m -> m.isReturnTypeOf(typeClass));

    }

    public static <S, T> SimpleStream<IObjectMethod<S, T>> getGetterMethods(Class<S> sourceClass) {
        Nulls.requireNonNull(sourceClass);
        return doClassInheritanceStream(sourceClass, n -> Stream.of(n.getDeclaredMethods()))
                .map(m -> (IObjectMethod<S, T>) ReflBase.makeObjectMethod(m))
                .filter(Nulls::nonNull)
                .filter(m -> m.isPublic() && m.isNotStatic() && m.getParameterCount() == 0)
                .filter(m -> {
                    if (m.nameIs("getClass")) {
                        return false;
                    }
                    if (m.isReturnTypeOf(Boolean.TYPE)) {
                        return m.nameStartsWith("is");
                    }
                    return m.nameStartsWith("get");
                });

    }

    public static <S, T> SimpleStream<IObjectMethod<S, T>> getGetterMethodsOfType(Class<S> sourceClass, Class<T> type) {
        Nulls.requireNonNulls(sourceClass, type);
        return ReflMethods.<S, T>getGetterMethods(sourceClass)
                .filter(method -> method.isReturnTypeOf(type));

    }

    public static <S> SimpleStream<IObjectMethod<S, Void>> getSetterMethods(Class<S> sourceClass) throws IntrospectionException {
        Nulls.requireNonNull(sourceClass);
        return doClassInheritanceStream(sourceClass, n -> Stream.of(n.getDeclaredMethods()))
                .map(m -> ReflBase.makeObjectMethod(m, sourceClass, Void.class))
                .filter(Nulls::nonNull)
                .filter(method -> method.isVoid() && method.isPublic() && method.getParameterCount() == 1)
                .filter(method -> {
                    return method.nameStartsWith("set");
                });

    }

}
