package lt.lb.commons.iteration.general.result;

/**
 *
 * @author laim0nas100
 */
public class IterIterableResult<T> {

    public final T val;
    public final int index;

    public IterIterableResult(int index, T val) {
        this.val = val;
        this.index = index;
    }

}
