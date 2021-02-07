package lt.lb.commons.iteration.general.cons.unchecked;

/**
 *
 * @author laim0nas100
 */
@FunctionalInterface
public interface IterIterableBiConsNoStopUnchecked<Type> extends IterIterableBiConsUnchecked<Type> {

    /**
     *
     * @param index
     * @param value
     * @return true = break, false = continue
     * @throws java.lang.Exception
     */
    @Override
    public default boolean uncheckedVisit(Integer index, Type value) throws Throwable {
        continuedVisit(index, value);
        return false;
    }

    public void continuedVisit(Integer index, Type value) throws Throwable;
}
