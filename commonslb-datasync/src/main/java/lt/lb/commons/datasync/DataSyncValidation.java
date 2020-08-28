package lt.lb.commons.datasync;

/**
 *
 * @author laim0nas100
 */
public interface DataSyncValidation<P, D, V extends Valid<P>> extends DataSync<P, D>, SyncValidation<P, V> {

}
