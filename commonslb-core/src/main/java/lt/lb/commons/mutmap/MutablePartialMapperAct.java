package lt.lb.commons.mutmap;

/**
 * Mutable mapper interface
 *
 * @author laim0nas100
 */
public interface MutablePartialMapperAct<From, To> {

    public void doMapping(From from, To to);
}
