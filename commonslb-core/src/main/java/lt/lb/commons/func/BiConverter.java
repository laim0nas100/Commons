package lt.lb.commons.func;

import java.util.Objects;
import java.util.function.Function;
import lt.lb.uncheckedutils.SafeOpt;
import lt.lb.uncheckedutils.func.UncheckedFunction;

/**
 *
 * @author laim0nas100
 */
public interface BiConverter<A, B> extends Function<A, B> {

    public B getFrom(A from);

    public A getBackFrom(B to);

    @Override
    public default B apply(A t) {
        return getFrom(t);
    }

    public default BiConverter<B, A> reverse() {
        BiConverter<A, B> me = this;
        return new BiConverter<B, A>() {
            @Override
            public A getFrom(B from) {
                return me.getBackFrom(from);
            }

            @Override
            public B getBackFrom(A to) {
                return me.getFrom(to);
            }
        };
    }

    public default <T> BiConverter<A, T> map(BiConverter<B, T> conv) {
        Objects.requireNonNull(conv);
        BiConverter<A, B> me = this;
        return new BiConverter<A, T>() {
            @Override
            public T getFrom(A from) {
                B to = me.getFrom(from);
                return conv.getFrom(to);
            }

            @Override
            public A getBackFrom(T to) {
                B backFrom = conv.getBackFrom(to);
                return me.getBackFrom(backFrom);
            }
        };
    }

    public static class StringIntegerConverter implements BiConverter<String, Integer> {

        @Override
        public Integer getFrom(String from) {
            return Integer.parseInt(from);
        }

        @Override
        public String getBackFrom(Integer to) {
            return String.valueOf(to);
        }

    }

    public static <T> BiConverter<T, T> identity() {
        return new BiConverter<T, T>() {
            @Override
            public T getFrom(T from) {
                return from;
            }

            @Override
            public T getBackFrom(T to) {
                return to;
            }
        };
    }

    public static <A, B> BiConverter<A, B> of(Function<A, B> from, Function<B, A> backFrom) {
        Objects.requireNonNull(from, "From function is null");
        Objects.requireNonNull(backFrom, "BackFrom function is null");

        return new BiConverter<A, B>() {
            @Override
            public B getFrom(A val) {
                return from.apply(val);
            }

            @Override
            public A getBackFrom(B to) {
                return backFrom.apply(to);
            }
        };
    }
    
    public static <A, B> BiConverter<SafeOpt<A>, SafeOpt<B>> ofSafe(UncheckedFunction<A, B> from, UncheckedFunction<B, A> backFrom) {
        Objects.requireNonNull(from, "From function is null");
        Objects.requireNonNull(backFrom, "BackFrom function is null");

        return new BiConverter<SafeOpt<A>, SafeOpt<B>>() {
            @Override
            public SafeOpt<B> getFrom(SafeOpt<A> val) {
                return val.map(from);
            }

            @Override
            public SafeOpt<A> getBackFrom(SafeOpt<B> to) {
                return to.map(backFrom);
            }
        };
    }

}
