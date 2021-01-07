package lt.lb.commons.misc.compare;

/**
 *
 * @author laim0nas100
 */
public class ComparatorBuilder<T> extends ComparatorBuilderBase<T, ComparatorBuilder<T>> {

    @Override
    protected ComparatorBuilder<T> makeBase() {
        return new ComparatorBuilder<>();
    }

    @Override
    protected ComparatorBuilder<T> me() {
        return this;
    }

}
