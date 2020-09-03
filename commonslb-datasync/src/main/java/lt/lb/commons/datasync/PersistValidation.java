package lt.lb.commons.datasync;

/**
 *
 * @author laim0nas100
 */
public interface PersistValidation<M, V extends Valid<M>> extends PurePersistValidation<M, V> {

    public void withPersistValidation(V validation);

}
