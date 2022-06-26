package lt.lb.commons.reflect.unified;

import java.util.stream.Stream;
import lt.lb.commons.Nulls;
import lt.lb.commons.iteration.streams.SimpleStream;

/**
 *
 * @author laim0nas100
 */
public abstract class ReflFields extends ReflBase {

    public static <S, T> SimpleStream<IField<S, T>> getFields(Class<S> cls) {
        Nulls.requireNonNulls(cls);
        return doClassInheritanceStream(cls, n -> Stream.of(n.getDeclaredFields()))
                .map(field -> {
                    return (IField<S, T>) () -> field;
                });

    }

    public static <S, T> SimpleStream<IObjectField<S, T>> getLocalFields(Class<S> cls) {
        Nulls.requireNonNulls(cls);
        return doClassInheritanceStream(cls, n -> Stream.of(n.getDeclaredFields()))
                .map(field -> {
                    return (IObjectField<S, T>) () -> field;
                })
                .filter(f -> f.isNotStatic());

    }

    public static <S, T> SimpleStream<IObjectField<S, T>> getLocalFields(Class<S> cls, Class<T> type) {
        Nulls.requireNonNulls(cls, type);
        return doClassInheritanceStream(cls, n -> Stream.of(n.getDeclaredFields()))
                .map(field -> {
                    return (IObjectField<S, T>) () -> field;
                })
                .filter(f -> f.isNotStatic() && f.isTypeOf(type));

    }

    public static <S, T> SimpleStream<IStaticField<S, T>> getStaticFields(Class<S> cls) {
        Nulls.requireNonNulls(cls);
        return doClassInheritanceStream(cls, n -> Stream.of(n.getDeclaredFields()))
                .<IStaticField<S, T>>map(field -> () -> field)
                .filter(f -> f.isStatic());

    }

    public static <S, T> SimpleStream<IStaticField<S, T>> getStaticFields(Class<S> cls, Class<T> type) {
        Nulls.requireNonNulls(cls, type);
        return doClassInheritanceStream(cls, n -> Stream.of(n.getDeclaredFields()))
                .map(field -> {
                    return (IStaticField<S, T>) () -> field;
                })
                .filter(f -> f.isStatic() && f.isTypeOf(type));

    }

    public static <S, T> SimpleStream<IStaticField<S, T>> getConstantFields(Class<S> sourceClass, Class<T> typeClass) {
        Nulls.requireNonNulls(sourceClass, typeClass);
        return doClassInheritanceStream(sourceClass, n -> Stream.of(n.getDeclaredFields()))
                .map(field -> {
                    return (IStaticField<S, T>) () -> field;
                })
                .filter(f -> f.isStatic() && f.isFinal() && f.isTypeOf(typeClass));

    }

}
