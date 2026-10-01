package com.github.laim0nas100.commonslb.datasync;

/**
 *
 * @author laim0nas100
 */
public interface DataSyncValidation<P, D, V extends Valid<P>> extends DataSync<P, D>, SyncValidation<P, V> {

}
