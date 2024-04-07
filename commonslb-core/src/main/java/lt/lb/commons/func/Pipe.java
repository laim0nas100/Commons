package lt.lb.commons.func;

import java.util.function.Function;
import java.util.function.Predicate;
import lt.lb.commons.F;
import lt.lb.commons.Nulls;
import lt.lb.commons.containers.values.Value;
import lt.lb.commons.containers.values.ValueProxy;

/**
 *
 * @author laim0nas100
 */
public interface Pipe<T> extends ValueProxy<T> {

    public <A> Pipe<A> construct(A value);

    public default <R> Pipe<R> apply(Function<? super T, ? extends R> func) {
        Nulls.requireNonNull(func);
        return construct(func.apply(get()));
    }

    public default <R, A> Pipe<R> apply(Lambda.L2R<? super T, ? super A, ? extends R> func, A a) {
        Nulls.requireNonNull(func);
        return construct(func.apply(get(), a));
    }

    public default <R, A, B> Pipe<R> apply(Lambda.L3R<? super T, ? super A, ? super B, ? extends R> func, A a, B b) {
        Nulls.requireNonNull(func);
        return construct(func.apply(get(), a, b));
    }

    public default <R, A, B, C> Pipe<R> apply(Lambda.L4R<? super T, ? super A, ? super B, ? super C, ? extends R> func, A a, B b, C c) {
        Nulls.requireNonNull(func);
        return construct(func.apply(get(), a, b, c));
    }

    public default boolean test(Predicate<T> pred) {
        Nulls.requireNonNull(pred);
        return pred.test(get());
    }

    public default <A> boolean test(Lambda.L2R<? super T, ? super A, Boolean> func, A a) {
        Nulls.requireNonNull(func);
        return func.apply(get(), a);
    }

    public default <A, B> boolean test(Lambda.L3R<? super T, ? super A, ? super B, Boolean> func, A a, B b) {
        Nulls.requireNonNull(func);
        return func.apply(get(), a, b);
    }

    public default <A, B, C> boolean test(Lambda.L4R<? super T, ? super A, ? super B, ? super C, Boolean> func, A a, B b, C c) {
        Nulls.requireNonNull(func);
        return func.apply(get(), a, b, c);
    }

    public static <T> Pipe<T> of(T value) {
        return new BasePipe<>(value);
    }

    public static <T> Pipe<T> mutable(T value) {
        return new MutablePipe<>(value);
    }

    public static class BasePipe<T> extends Value<T> implements Pipe<T> {

        public BasePipe(T val) {
            super(val);
        }

        @Override
        public <A> Pipe<A> construct(A value) {
            return new BasePipe<>(value);
        }

    }

    public static class MutablePipe<T> extends BasePipe<T> {

        public MutablePipe(T val) {
            super(val);
        }

        @Override
        public <A> Pipe<A> construct(A value) {
            Pipe me = this;
            me.set(value);
            return F.cast(me);
        }

    }
}
