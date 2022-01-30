package lt.lb.commons.reflect;

/**
 *
 * @author laim0nas100
 */
public interface IExplicitClone<T> {

    public T clone(FieldFactory factory, T value);
}
