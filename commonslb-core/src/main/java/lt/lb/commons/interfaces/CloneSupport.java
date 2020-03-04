package lt.lb.commons.interfaces;

/**
 * Interface for cloning, explicitly declaring clone method publicly.
 * @author laim0nas100
 */
public interface CloneSupport<T> extends Cloneable {

    public T clone();
}
