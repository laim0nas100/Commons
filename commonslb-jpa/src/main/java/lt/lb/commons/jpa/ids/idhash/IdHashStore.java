package lt.lb.commons.jpa.ids.idhash;

/**
 *
 * @author laim0nas100
 */
public interface IdHashStore {

    /**
     * Register local hash to be used instead of given id.
     *
     * @param id
     * @param localHash
     * @return registered localHash at given id
     */
    public Object register(Object id, Object localHash);

    /**
     * Get local hash registered at given id or null.
     *
     * @param id
     * @return
     */
    public Object getMapping(Object id);

}
