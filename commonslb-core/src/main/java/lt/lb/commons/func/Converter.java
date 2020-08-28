package lt.lb.commons.func;

/**
 *
 * @author laim0nas100
 */
public interface Converter<From, To> {

    public To getFrom(From from);

    public From getBackFrom(To to);

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
