package lt.lb.commons.jpa.ids.idhash;

/**
 *
 * NO operation implementation. Using this will result in 2 possible different
 * hashCodes. Local one when ID is not set, and then ID-based one, when ID is
 * eventually set.
 *
 * @author laim0nas100
 */
public class IdHashStoreNOP implements IdHashStore {

    @Override
    public Object register(Object id, Object localHash) {
        return null;
    }

    @Override
    public Object getMapping(Object id) {
        return null;
    }

}
