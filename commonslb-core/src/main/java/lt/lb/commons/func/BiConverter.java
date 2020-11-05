package lt.lb.commons.func;

import java.util.function.Function;
import lt.lb.commons.parsing.NumberParsing;

/**
 *
 * @author laim0nas100
 */
public interface BiConverter<From, To> extends Function<From,To> {

    public To getFrom(From from);

    public From getBackFrom(To to);

    @Override
    public default To apply(From t) {
        return getFrom(t);
    }

    public default BiConverter<To, From> reverse() {
        BiConverter<From, To> me = this;
        return new BiConverter<To, From>() {
            @Override
            public From getFrom(To from) {
                return me.getBackFrom(from);
            }

            @Override
            public To getBackFrom(From to) {
                return me.getFrom(to);
            }
        };
    }

    public default <T> BiConverter<From, T> map(BiConverter<To, T> conv) {
        BiConverter<From, To> me = this;
        return new BiConverter<From, T>() {
            @Override
            public T getFrom(From from) {
                To to = me.getFrom(from);
                return conv.getFrom(to);
            }

            @Override
            public From getBackFrom(T to) {
                To backFrom = conv.getBackFrom(to);
                return me.getBackFrom(backFrom);
            }
        };
    }

    public static class StringIntegerConverter implements BiConverter<String, Integer> {

        @Override
        public Integer getFrom(String from) {
            return NumberParsing.parseInt(from).orElse(null);
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

}
