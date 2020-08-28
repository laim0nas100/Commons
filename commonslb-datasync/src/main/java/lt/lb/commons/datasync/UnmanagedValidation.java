package lt.lb.commons.datasync;

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
