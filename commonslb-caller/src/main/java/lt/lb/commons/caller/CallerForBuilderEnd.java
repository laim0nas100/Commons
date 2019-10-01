package lt.lb.commons.caller;

/**
 *
 * @author laim0nas100
 */
public interface CallerForBuilderEnd<R,T> {
    /**
     * @param afterwards Caller when iterator runs out of items (or never had
     * them to begin with) and {@code for} loop never exited inside.
     * @return Caller instance of such {@code for} loop
     */
    public Caller<T> afterwards(Caller<T> afterwards);
}
