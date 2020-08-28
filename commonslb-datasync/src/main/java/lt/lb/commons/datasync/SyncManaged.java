package lt.lb.commons.datasync;

import lt.lb.commons.containers.values.ValueProxy;

/**
 *
 * @author laim0nas100
 */
public interface SyncManaged<M> extends ValueProxy<M> {

    public void setManaged(M managed);

    public M getManaged();

    @Override
    public default void set(M v) {
        setManaged(v);
    }

    @Override
    public default M get() {
        return getManaged();
    }

}
