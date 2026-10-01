package com.github.laim0nas100.commonslb.datasync.base;

import com.github.laim0nas100.commonslb.datasync.Valid;


/**
 *
 * @author laim0nas100
 */
public abstract class GenDataSync<P, D, V extends Valid<P>> extends ExplicitDataSync<P, P, D, V> {

    public GenDataSync() {
    }

}
