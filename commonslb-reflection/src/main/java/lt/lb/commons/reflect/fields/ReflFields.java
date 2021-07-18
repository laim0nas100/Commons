package lt.lb.commons.reflect.fields;

import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;
import lt.lb.commons.Ins.InsCl;

/**
 *
 * @author laim0nas100
 */
public abstract class ReflFields {

    public static <T> Stream<T> doClassSuperIterationStream(Class node, Function<Class, Stream<T>> func) {
        Objects.requireNonNull(node);
        Objects.requireNonNull(func);
        Stream<T> st = Stream.empty();
        while (node != null) {
            st = Stream.concat(st, func.apply(node));
            node = node.getSuperclass();
        }
        return st;
    }

    public static <S> Stream<IField<S, ?>> getFieldsOf(Class<S> cls) {
        return doClassSuperIterationStream(cls, n -> Stream.of(n.getDeclaredFields()))
                .map(field -> {
                    return (IField<S, ?>) () -> field;
                });

    }

    public static <S, T> Stream<IField<S, T>> getFieldsOfType(Class<S> sourceClass, InsCl<T> typeClass) {
        Objects.requireNonNull(sourceClass);
        Objects.requireNonNull(typeClass);
        return doClassSuperIterationStream(sourceClass, n -> Stream.of(n.getDeclaredFields()))
                .filter(field -> typeClass.instanceOf(field.getType()))
                .map(field -> {
                    return (IField<S, T>) () -> field;
                });

    }

    public static <S, T> Stream<IStaticField<S, T>> getConstantFields(Class<S> sourceClass, InsCl<T> typeClass) {
        Objects.requireNonNull(sourceClass);
        Objects.requireNonNull(typeClass);
        return doClassSuperIterationStream(sourceClass, n -> Stream.of(n.getDeclaredFields()))
                .filter(field -> typeClass.instanceOf(field.getType()))
                .map(field -> {
                    return (IField<S, T>) () -> field;
                })
                .filter(f -> f.isStatic() && f.isFinal())
                .map(f -> f.asStaticField());

    }

}
