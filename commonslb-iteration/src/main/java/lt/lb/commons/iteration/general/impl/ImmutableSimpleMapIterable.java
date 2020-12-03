package lt.lb.commons.iteration.general.impl;

/**
 *
 * @author laim0nas100
 */
public class ImmutableSimpleMapIterable extends SimpleMapIterable {

    @Override
    protected SimpleMapIterable me() {
        return new SimpleMapIterable();
    }

    @Override
    public SimpleMapIterable last(int amountToInclude) {
        return new SimpleMapIterable().last(amountToInclude);
    }

    @Override
    public SimpleMapIterable first(int amountToInclude) {
        return new SimpleMapIterable().first(amountToInclude);
    }

}
