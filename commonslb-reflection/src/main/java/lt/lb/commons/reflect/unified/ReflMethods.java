package lt.lb.commons.reflect.unified;

import java.beans.IntrospectionException;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import lt.lb.commons.Equator;
import lt.lb.commons.Nulls;
import lt.lb.commons.iteration.streams.SimpleStream;

/**
 *
 * @author laim0nas100
 */
public abstract class ReflMethods extends ReflBase {

    /**
     * Checks if method is {@link Object#getClass() }
     *
     * @param method
     * @return
     */
    public static boolean isGetClassMethod(IMethod method) {
        return method.matchesSignature(Class.class, "getClass");
    }

    /**
     * Checks if method is {@link Object#hashCode() }
     *
     * @param method
     * @return
     */
    public static boolean isHashCodeMethod(IMethod method) {
        return method.matchesSignature(Integer.TYPE, "hashCode");
    }

    /**
     * Checks if method is {@link Object#toString() }
     *
     * @param method
     * @return
     */
    public static boolean isToStringMethod(IMethod method) {
        return method.matchesSignature(String.class, "toString");
    }

    /**
     * Checks if method is {@link Object#equals(java.lang.Object) }
     *
     * @param method
     * @return
     */
    public static boolean isEqualsMethod(IMethod method) {
        return method.matchesSignature(Boolean.TYPE, "equals", Object.class);
    }

    /**
     * Checks if method is on of the following {@link Object#wait()  } ,
     * {@link Object#wait()  },{@link Object#wait(long, int)   },{@link Object#notify()  }, {@link Object#notifyAll()
     * }
     *
     * @param method
     * @return
     */
    public static boolean isSyncronizationMethod(IMethod method) {

        //must return void
        if (!method.isVoid()) {
            return false;
        }
        String name = method.getName();
        if (method.hasNoParameters() && (name.equals("notify") || name.equals("notifyAll"))) {
            return true;
        }
        if (name.equals("wait")) {
            return method.matchesParameters(Long.TYPE) || method.matchesParameters(Long.TYPE, Integer.TYPE);
        }

        return false;
    }

    public static boolean isBaseObjectMethod(IMethod method) {
        return isEqualsMethod(method) || isHashCodeMethod(method) || isGetClassMethod(method) || isToStringMethod(method) || isSyncronizationMethod(method);
    }

    public static <S> SimpleStream<IMethod<S, Object>> getMethods(Class<S> sourceClass) {
        return getMethodsTyped(sourceClass);
    }

    public static <S, T> SimpleStream<IMethod<S, T>> getMethodsTyped(Class<S> sourceClass) {
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

    public static <S> SimpleStream<IStaticMethod<S, Object>> getStaticMethods(Class<S> sourceClass) {
        return getStaticMethodsTyped(sourceClass);
    }

    public static <S, T> SimpleStream<IStaticMethod<S, T>> getStaticMethodsTyped(Class<S> sourceClass) {
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

    public static <S> SimpleStream<IObjectMethod<S, Object>> getLocalMethods(Class<S> sourceClass) {
        return getLocalMethodsTyped(sourceClass);
    }

    public static <S, T> SimpleStream<IObjectMethod<S, T>> getLocalMethodsTyped(Class<S> sourceClass) {
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

    public static final Pattern GET_PATTERN = Pattern.compile("^get[A-Z].*");
    public static final Pattern IS_PATTERN = Pattern.compile("^is[A-Z].*");
    public static final Pattern SET_PATTERN = Pattern.compile("^set[A-Z].*");

    public static <S> SimpleStream<IObjectMethod<S, Object>> getGetterMethods(Class<S> sourceClass) {
        return getGetterMethodsTyped(sourceClass);
    }

    public static <S, T> SimpleStream<IObjectMethod<S, T>> getGetterMethodsTyped(Class<S> sourceClass) {
        Nulls.requireNonNull(sourceClass);
        return doClassInheritanceStream(sourceClass, n -> Stream.of(n.getDeclaredMethods()))
                .map(m -> (IObjectMethod<S, T>) ReflBase.makeObjectMethod(m))
                .filter(Nulls::nonNull)
                .filter(m -> m.isPublic() && m.isNotStatic() && m.hasNoParameters())
                .remove(ReflMethods::isGetClassMethod)
                .filter(m -> {
                    if (m.isReturnTypeOf(Boolean.TYPE)) {
                        return m.nameMatches(IS_PATTERN);
                    }
                    return m.nameMatches(GET_PATTERN);
                })
                .sequential()
                .distinct(Equator.valueHashEquator(r -> r.getName()));// getters are allowed to override

    }

    public static <S, T> SimpleStream<IObjectMethod<S, T>> getGetterMethodsOfType(Class<S> sourceClass, Class<T> type) {
        Nulls.requireNonNulls(sourceClass, type);
        return ReflMethods.<S, T>getGetterMethodsTyped(sourceClass)
                .filter(method -> method.isReturnTypeOf(type));

    }

    public static <S> SimpleStream<IObjectMethod<S, Void>> getSetterMethods(Class<S> sourceClass) throws IntrospectionException {
        Nulls.requireNonNull(sourceClass);
        return doClassInheritanceStream(sourceClass, n -> Stream.of(n.getDeclaredMethods()))
                .map(m -> ReflBase.makeObjectMethod(m, sourceClass, Void.class))
                .filter(Nulls::nonNull)
                .filter(method -> method.isVoid() && method.isPublic() && method.getParameterCount() == 1)
                .filter(method -> method.nameMatches(SET_PATTERN))
                .distinct(Equator.valueHashEquator(r -> r.getName()));// setters are allowed to override;

    }

}
