package lt.lb.commons.iteration.general.impl.unchecked;

/**
 *
 * @author laim0nas100
 */
public class ImmutableSimpleMapIterableUnchecked extends SimpleMapIterableUnchecked {

    @Override
    protected SimpleMapIterableUnchecked me() {
        return new SimpleMapIterableUnchecked();
    }

    @Override
    public SimpleMapIterableUnchecked last(int amountToInclude) {
        return new SimpleMapIterableUnchecked().last(amountToInclude);
    }

    @Override
    public SimpleMapIterableUnchecked first(int amountToInclude) {
        return new SimpleMapIterableUnchecked().first(amountToInclude);
    }

}
