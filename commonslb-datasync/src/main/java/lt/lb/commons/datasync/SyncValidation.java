package lt.lb.commons.datasync;

/**
 *
 * @author laim0nas100
 */
public interface SyncValidation<M, V extends Valid<M>>
        extends DisplayValidation<M, V>, PersistValidation<M, V> {
}
