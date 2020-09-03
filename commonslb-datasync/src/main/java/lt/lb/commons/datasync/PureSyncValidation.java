package lt.lb.commons.datasync;

/**
 *
 * @author laim0nas100
 */
public interface PureSyncValidation<M, V extends Valid<M>>
        extends PurePersistAndDisplayValidation<M,V>, SyncDisplay, SyncPersist {
}
