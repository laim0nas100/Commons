package com.github.laim0nas100.commonslb.datasync;

/**
 *
 * @author laim0nas100
 */
public interface PureSyncValidation<M, V extends Valid<M>>
        extends PurePersistAndDisplayValidation<M,V>, SyncDisplay, SyncPersist {
}
