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
                .map(ReflBase::makeField);

    }

    public static <S, T> SimpleStream<IObjectField<S, T>> getLocalFields(Class<S> cls) {
        Nulls.requireNonNulls(cls);
        return doClassInheritanceStream(cls, n -> Stream.of(n.getDeclaredFields()))
                .map(f -> (IObjectField<S, T>) ReflBase.makeObjectField(f, cls, null))
                .filter(Nulls::nonNull);

    }

    public static <S, T> SimpleStream<IObjectField<S, T>> getLocalFields(Class<S> cls, Class<T> type) {
        Nulls.requireNonNulls(cls);
        return doClassInheritanceStream(cls, n -> Stream.of(n.getDeclaredFields()))
                .map(f -> makeObjectField(f, cls, type))
                .filter(Nulls::nonNull)
                .filter(f -> f.isTypeOf(type));

    }

    public static <S, T> SimpleStream<IStaticField<S, T>> getStaticFields(Class<S> cls) {
        Nulls.requireNonNulls(cls);
        return doClassInheritanceStream(cls, n -> Stream.of(n.getDeclaredFields()))
                .map(f -> (IStaticField<S, T>) ReflBase.makeStaticField(f, cls, null))
                .filter(Nulls::nonNull)
                .filter(f -> f.isStatic());

    }

    public static <S, T> SimpleStream<IStaticField<S, T>> getStaticFields(Class<S> cls, Class<T> type) {
        Nulls.requireNonNulls(cls, type);
        return doClassInheritanceStream(cls, n -> Stream.of(n.getDeclaredFields()))
                .map(f -> ReflBase.makeStaticField(f, cls, type))
                .filter(Nulls::nonNull)
                .filter(f -> f.isTypeOf(type));

    }

    public static <S, T> SimpleStream<IStaticField<S, T>> getConstantFields(Class<S> cls, Class<T> type) {
        Nulls.requireNonNulls(cls, type);
        return doClassInheritanceStream(cls, n -> Stream.of(n.getDeclaredFields()))
                .map(f -> ReflBase.makeStaticField(f, cls, type))
                .filter(Nulls::nonNull)
                .filter(f -> f.isFinal() && f.isTypeOf(type));

    }

}
