package lt.lb.commons.iteration.general.cons;

/**
 *
 * @author laim0nas100
 */
@FunctionalInterface
public interface IterIterableBiConsNoStop<Type> extends IterIterableBiCons<Type> {

    /**
     *
     * @param index
     * @param value
     * @return true = break, false = continue
     */
    @Override
    public default boolean visit(Integer index, Type value) {
        continuedVisit(index, value);
        return false;
    }

    public void continuedVisit(Integer index, Type value);
}
