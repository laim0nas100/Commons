package lt.lb.commons.func;

import java.util.function.Function;
import lt.lb.commons.parsing.NumberParsing;

/**
 *
 * @author laim0nas100
 */
public interface Converter<From, To> {

    public To getFrom(From from);

    public From getBackFrom(To to);

    public default Converter<To, From> reverse() {
        Converter<From, To> me = this;
        return new Converter<To, From>() {
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

    public default <T> Converter<From, T> map(Converter<To, T> conv) {
        Converter<From, To> me = this;
        return new Converter<From, T>() {
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

    public static class StringIntegerConverter implements Converter<String, Integer> {

        @Override
        public Integer getFrom(String from) {
            return NumberParsing.parseInt(from).orElse(null);
        }

        @Override
        public String getBackFrom(Integer to) {
            return String.valueOf(to);
        }

    }

    public static <T> Converter<T, T> identity() {
        return new Converter<T, T>() {
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
