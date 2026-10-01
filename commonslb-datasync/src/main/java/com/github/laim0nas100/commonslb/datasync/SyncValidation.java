package com.github.laim0nas100.commonslb.datasync;

/**
 *
 * @author laim0nas100
 */
public interface SyncValidation<M, V extends Valid<M>>
        extends PersistAndDisplayValidation<M, V>, SyncDisplay, SyncPersist {
}
