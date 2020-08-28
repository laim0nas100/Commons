package lt.lb.commons.datasync;

/**
 *
 * @author laim0nas100
 */
public interface DataSyncManagedValidation<P, M, D, V extends Valid<M>> extends DataSyncManaged<P, M, D>, SyncValidation<M, V> {

}
