package lt.lb.commons.copy;

/**
 * Mutable mapper interface
 *
 * @author laim0nas100
 */
public interface MutablePartialMapperAct<From, To> {

    public void doMapping(From from, To to);
}
