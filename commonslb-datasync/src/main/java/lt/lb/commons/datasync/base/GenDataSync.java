package lt.lb.commons.datasync.base;

import lt.lb.commons.datasync.Valid;


/**
 *
 * @author laim0nas100
 */
public abstract class GenDataSync<P, D, V extends Valid<P>> extends ExplicitDataSync<P, P, D, V> {

    public GenDataSync() {
    }

}
