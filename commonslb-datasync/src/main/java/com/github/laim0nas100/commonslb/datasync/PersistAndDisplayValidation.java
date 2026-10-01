package com.github.laim0nas100.commonslb.datasync;

/**
 *
 * @author laim0nas100
 */
public interface PersistAndDisplayValidation<M, V extends Valid<M>>
        extends DisplayValidation<M, V>, PersistValidation<M, V>, PurePersistAndDisplayValidation<M, V> {
}
