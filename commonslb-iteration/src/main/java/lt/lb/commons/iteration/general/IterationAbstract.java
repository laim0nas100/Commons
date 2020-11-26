package lt.lb.commons.iteration.general;

/**
 *
 * @author laim0nas100
 */
public interface IterationAbstract<E extends IterationAbstract<E>> {

    public E first(int amountToInclude);

    public E last(int amountToInclude);

}
