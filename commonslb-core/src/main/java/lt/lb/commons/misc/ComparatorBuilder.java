package lt.lb.commons.misc;

/**
 *
 * @author Laimonas-Beniusis-PC
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
