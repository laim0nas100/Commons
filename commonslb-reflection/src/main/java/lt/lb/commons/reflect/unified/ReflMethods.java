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

    public static <S> SimpleStream<IMethod<S, ?>> getMethodsOf(Class<S> cls) {
        return doClassSuperIterationStream(cls, n -> Stream.of(n.getDeclaredMethods()))
                .map(field -> {
                    return (IMethod<S, ?>) () -> field;
                });

    }

    public static <S, T> SimpleStream<IMethod<S, T>> getMethodsOfType(Class<S> sourceClass, Class<T> typeClass) {
        Nulls.requireNonNulls(sourceClass, typeClass);
        return doClassSuperIterationStream(sourceClass, n -> Stream.of(n.getDeclaredMethods()))
                .map(method -> {
                    return (IMethod<S, T>) () -> method;
                })
                .filter(m-> m.isReturnTypeOf(typeClass));

    }

    public static <S, T> SimpleStream<IStaticMethod<S, T>> getStaticMethods(Class<S> sourceClass, Class<T> typeClass) {
        Nulls.requireNonNulls(sourceClass, typeClass);
        return doClassSuperIterationStream(sourceClass, n -> Stream.of(n.getDeclaredMethods()))
                .map(m -> {
                    return (IStaticMethod<S, T>) () -> m;
                })
                .filter(m -> m.isStatic())
                .filter(method -> method.isReturnTypeOf(sourceClass));
                
                

    }

    public static <S,T> SimpleStream<IObjectMethod<S, T>> getGetterMethods(Class<S> sourceClass) {
        Nulls.requireNonNull(sourceClass);
        return doClassSuperIterationStream(sourceClass, n -> Stream.of(n.getDeclaredMethods()))
                .map(m -> {
                    return (IObjectMethod<S, T>) () -> m;
                })
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
        Nulls.requireNonNull(type);
        return getGetterMethods(sourceClass).filter(method -> method.isReturnTypeOf(type)).map(m -> (IObjectMethod<S, T>) m);

    }

    public static <S> SimpleStream<IObjectMethod<S, Void>> getSetterMethods(Class<S> sourceClass) throws IntrospectionException {
        Nulls.requireNonNull(sourceClass);
        return doClassSuperIterationStream(sourceClass, n -> Stream.of(n.getDeclaredMethods()))
                .map(method -> {
                    return (IObjectMethod<S, Void>) () -> method;
                })
                .filter(method -> method.isVoid() && method.isPublic() && method.getParameterCount() == 1)
                .filter(method -> {
                    return method.nameStartsWith("set");
                });

    }

}
