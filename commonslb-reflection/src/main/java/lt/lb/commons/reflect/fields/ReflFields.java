package lt.lb.commons.reflect.fields;

import java.util.function.Function;
import java.util.stream.Stream;
import lt.lb.commons.Ins.InsCl;
import lt.lb.commons.Nulls;
import lt.lb.commons.iteration.streams.MakeStream;
import lt.lb.commons.iteration.streams.SimpleStream;

/**
 *
 * @author laim0nas100
 */
public abstract class ReflFields {

    public static <T> SimpleStream<T> doClassSuperIterationStream(Class node, Function<Class, Stream<T>> func) {
        Nulls.requireNonNulls(node, func);
        SimpleStream<T> st = MakeStream.fromValues();
        while (node != null) {
            st.append(func.apply(node));
            node = node.getSuperclass();
        }
        return st;
    }

    public static <S> SimpleStream<IField<S, ?>> getFieldsOf(Class<S> cls) {
        return doClassSuperIterationStream(cls, n -> Stream.of(n.getDeclaredFields()))
                .map(field -> {
                    return (IField<S, ?>) () -> field;
                });

    }

    public static <S, T> SimpleStream<IField<S, T>> getFieldsOfType(Class<S> sourceClass, InsCl<T> typeClass) {
        Nulls.requireNonNulls(sourceClass, typeClass);
        return doClassSuperIterationStream(sourceClass, n -> Stream.of(n.getDeclaredFields()))
                .filter(field -> typeClass.instanceOf(field.getType()))
                .map(field -> {
                    return (IField<S, T>) () -> field;
                });

    }

    public static <S, T> SimpleStream<IStaticField<S, T>> getConstantFields(Class<S> sourceClass, InsCl<T> typeClass) {
        Nulls.requireNonNulls(sourceClass, typeClass);
        return doClassSuperIterationStream(sourceClass, n -> Stream.of(n.getDeclaredFields()))
                .filter(field -> typeClass.instanceOf(field.getType()))
                .map(field -> {
                    return (IField<S, T>) () -> field;
                })
                .filter(f -> f.isStatic() && f.isFinal())
                .map(f -> f.asStaticField());

    }

}
