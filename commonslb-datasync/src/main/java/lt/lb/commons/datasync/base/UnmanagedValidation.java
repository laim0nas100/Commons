package lt.lb.commons.datasync.base;

import lt.lb.commons.datasync.Valid;

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
