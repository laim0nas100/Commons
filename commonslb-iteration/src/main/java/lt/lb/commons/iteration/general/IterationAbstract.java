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

    /**
     * Set staring index (inclusive)
     *
     * @param from inclusive starting interval index
     * @return
     */
    public E startingFrom(int from);

    /**
     * Set ending index (exclusive)
     *
     * @param to exclusive ending interval index
     * @return
     */
    public E endingBefore(int to);

    /**
     * Set interval. [from , to). Inclusive : Exclusive.
     *
     * @param from
     * @param to
     * @return
     */
    public default E withInterval(int from, int to) {
        return startingFrom(from).endingBefore(to);
    }

}
