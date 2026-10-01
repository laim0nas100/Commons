package com.github.laim0nas100.commonslb.datasync.base;

import com.github.laim0nas100.commonslb.datasync.Valid;

/**
 *
 * @author laim0nas100
 */
public class UnmanagedValidation<V extends Valid<Object>> extends BaseValidation<Object, V> {

    @Override
    public Object getManaged() {
        return null;
    }

}
