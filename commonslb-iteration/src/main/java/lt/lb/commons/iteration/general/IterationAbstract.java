package lt.lb.commons.iteration.general;

/**
 *
 * @author laim0nas100
 */
public interface IterationAbstract<E extends IterationAbstract<E>> {

    /**
     * Iterate only through first elements in the given interval
     *
     * @param amountToInclude
     * @return
     */
    public E first(int amountToInclude);

    /**
     * Iterate only through last elements in the given interval
     *
     * @param amountToInclude
     * @return
     */
    public E last(int amountToInclude);

}
