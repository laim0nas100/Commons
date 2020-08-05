package lt.lb.commons.func;

/**
 *
 * @author laim0nas100
 */
public interface Converter<From, To> {

    public To getFrom(From from);

    public From getBackFrom(To to);

}
