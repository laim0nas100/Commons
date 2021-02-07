package lt.lb.commons.iteration.general.cons.unchecked;

import lt.lb.commons.iteration.general.result.IterMapResult;

/**
 *
 * @author laim0nas100
 */
@FunctionalInterface
public interface IterMapConsNoStopUnchecked<K, V> extends IterMapConsUnchecked<K, V> {

    /**
     *
     * @param entry
     * @return true = break, false = continue
     * @throws java.lang.Exception
     */
    @Override
    public default boolean uncheckedVisit(IterMapResult<K, V> entry) throws Throwable {
        continuedVisit(entry);
        return false;
    }

    public void continuedVisit(IterMapResult<K, V> entry) throws Throwable;
}
