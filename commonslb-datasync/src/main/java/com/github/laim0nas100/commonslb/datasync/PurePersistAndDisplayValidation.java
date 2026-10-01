package com.github.laim0nas100.commonslb.datasync;

/**
 *
 * @author laim0nas100
 */
public interface PurePersistAndDisplayValidation <M, V extends Valid<M>>
        extends PureDisplayValidation<M, V>, PurePersistValidation<M, V>{
}
